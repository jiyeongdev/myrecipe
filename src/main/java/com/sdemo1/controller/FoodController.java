package com.sdemo1.controller;

import com.sdemo1.domain.FoodCategoryDto;
import com.sdemo1.domain.FoodCategoryResponse;
import com.sdemo1.domain.FoodItem;
import com.sdemo1.domain.RecipeDto;
import com.sdemo1.domain.RecipeStepDto;
import com.sdemo1.repository.FoodRepository;
import com.sdemo1.service.FoodService;
import jakarta.annotation.PostConstruct;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static com.sdemo1.common.utils.ValidateUtils.isValidParam;


@RestController
@RequestMapping("/food")
public class FoodController {

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    private S3Client s3Client;

    @PostConstruct
    public void initializeS3Client() {
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();
    }

    @PostMapping("/test")
    public String test(){
        return "eeee";
    }

    @Autowired
    FoodService foodService;

    @GetMapping("/category")
    public List<FoodItem> getMainIngredient(){
        List<FoodItem> foodItems = foodService.findByParentIDIn(Arrays.asList("P","R"));
        System.out.println("대분류 : " + foodItems);
        return foodItems;
    }


    @GetMapping("/allCategory")
    public FoodCategoryResponse findFoodCategory() {
        List<FoodItem> foodItems = foodService.findFoodCategory();
        
        // 서브 카테고리 맵 생성
        Map<String, List<FoodItem>> subMapCategory = foodItems.stream()
                .filter(obj -> !("P".equals(obj.getParentID()) || "R".equals(obj.getParentID())))
                .collect(Collectors.groupingBy(FoodItem::getParentID));

        // 메인 카테고리 리스트 생성
        List<FoodItem> mainMapCategory = foodItems.stream()
                .filter(obj -> "P".equals(obj.getParentID()) || "R".equals(obj.getParentID()))
                .collect(Collectors.toList()); // List<FoodItem>으로 변환

        // FoodCategoryResponse 생성
        FoodCategoryResponse foodCategoryData = new FoodCategoryResponse(mainMapCategory, subMapCategory);

        return foodCategoryData;
    }

    @GetMapping("/ingredient")
    public  ResponseEntity<?>  findIngredient(@RequestParam Map<String, String> cIDs){
        if ("".equals(cIDs.get("mID")) || "".equals(cIDs.get("sID"))) {
            return ResponseEntity.badRequest().body("mID 또는 sID 키는 존재하나 값이 비어있습니다.");
        }

        return foodService.findIngredientByID(cIDs);
    }

    @GetMapping("/ingredient-all")
    public ResponseEntity<?> allIngredient(@RequestParam Integer lastID , @RequestParam(defaultValue = "10") int size){
        if (lastID ==null){
            lastID = 0;
        }
        return foodService.allIngredient(lastID, size);
    }

    
    @GetMapping("/sub-ingredient")
    public List<FoodItem> getSubIngredient(@RequestParam("parentID") String parentID){
        List<FoodItem> foodItems = foodService.GetByParentID(parentID);
        System.out.println("parentID : " + parentID +" 에 대한 하위분류 "+foodItems);
        return foodItems;
    }

    @PostMapping("/recipe")
    public ResponseEntity<String> uploadRecipe(
            @RequestPart(value = "recipe" , required = false) RecipeDto recipeDto,  // JSON 데이터
            @RequestParam("files") List<MultipartFile> files) {  // 업로드 파일들

        System.out.println("=====================");
        System.out.println(recipeDto);
        // 데이터 검증
        if (recipeDto.getSteps() == null || recipeDto.getSteps().isEmpty() ||
                recipeDto.getTitle() == null || recipeDto.getUserID() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Title, UserID, 또는 Steps가 누락되었습니다.");
        }

        try {
            // 업로드된 S3 URL을 저장할 리스트
            List<String> uploadedUrls = new ArrayList<>();
            for (int i = 0; i < recipeDto.getSteps().size(); i++) {
                RecipeStepDto step = recipeDto.getSteps().get(i);

                // 파일 가져오기
                MultipartFile file = files.get(i);
                String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();

                // S3에 업로드
                s3Client.putObject(PutObjectRequest.builder()
                                .bucket(bucketName)
                                .key(fileName)
                                .contentType(file.getContentType())
                                .build(),
                        RequestBody.fromBytes(file.getBytes()));

                // S3 URL 생성
                String fileUrl = "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + fileName;
                uploadedUrls.add(fileUrl);

                // Step에 업로드된 URL 설정
//                step.setPhotoFile(fileUrl);
                System.out.println("fileURL : "+ fileUrl);
            }

            // 여기에서 recipeDto와 S3 URL 데이터를 DB에 저장하거나 추가 작업을 수행

            return ResponseEntity.ok("레시피 업로드 성공!");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 업로드 실패: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("에러 발생: " + e.getMessage());
        }
    }

    @PostMapping("/upload-single")
    public ResponseEntity<String> uploadSingleFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty");
        }

        // 파일 저장 위치 설정
        String uploadDir = System.getProperty("user.dir") + "/uploads/";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            // 파일 저장
            String filePath = uploadDir + file.getOriginalFilename();
            file.transferTo(new File(filePath));
            return ResponseEntity.ok("File uploaded successfully: " + filePath);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file: " + e.getMessage());
        }
    }


    // 재료선택 화면 API
    @GetMapping("/findIngredientByFilter")
    public ResponseEntity<?>  findIngredientByFilter(@RequestParam Map<String, String> params){
        return foodService.findIngredientByFilter(params);
    }

}
