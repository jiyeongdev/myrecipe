package com.sdemo1.controller;

import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.dto.FoodCategoryResponse;
import com.sdemo1.dto.RecipeDto;
import com.sdemo1.dto.RecipeStepDto;
import com.sdemo1.entity.FoodItem;
import com.sdemo1.exception.CustomException;
import com.sdemo1.service.FoodService;
import com.sdemo1.common.utils.ValidateUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.sdemo1.common.utils.ValidateUtils.isValidParam;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

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
    private FoodService foodService;

    @GetMapping("/category")
    public ApiResponse<List<FoodItem>> getMainIngredient(){
        List<FoodItem> foodItems = foodService.findByParentIDIn(Arrays.asList("P","R"));
        System.out.println("대분류 : " + foodItems);
        return new ApiResponse<>(true, "성공", foodItems, HttpStatus.OK);
    }

    @GetMapping("/allCategory")
    public ApiResponse<FoodCategoryResponse> findFoodCategory() {
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

        return new ApiResponse<>(true, "성공", foodCategoryData, HttpStatus.OK);
    }

    @GetMapping("/ingredient")
    public ApiResponse<?> findIngredient(@RequestParam Map<String, String> cIDs) {
        if (!isValidParam(cIDs.get("mID")) || !ValidateUtils.isValidParam(cIDs.get("sID"))) {
            throw new CustomException("mID 또는 sID 키는 존재하나 값이 비어있습니다.", HttpStatus.BAD_REQUEST.value());
        }

        return new ApiResponse<>(true, "성공", foodService.findIngredientByID(cIDs), HttpStatus.OK);
    }

    @GetMapping("/ingredient-all")
    public ApiResponse<?> allIngredient(@RequestParam Integer lastID, @RequestParam(defaultValue = "10") int size) {
        if (lastID == null) {
            lastID = 0;
        }
        return new ApiResponse<>(true, "성공", foodService.allIngredient(lastID, size), HttpStatus.OK);
    }

    @GetMapping("/sub-ingredient")
    public ApiResponse<List<FoodItem>> getSubIngredient(@RequestParam("parentID") String parentID){
        List<FoodItem> foodItems = foodService.GetByParentID(parentID);
        System.out.println("parentID : " + parentID +" 에 대한 하위분류 "+foodItems);
        return new ApiResponse<>(true, "성공", foodItems, HttpStatus.OK);
    }

    @PostMapping("/recipe")
    public ApiResponse<String> uploadRecipe(
            @RequestPart(value = "recipe", required = false) RecipeDto recipeDto,  // JSON 데이터
            @RequestParam("files") List<MultipartFile> files) {  // 업로드 파일들

        System.out.println("=====================");
        System.out.println(recipeDto);
        // 데이터 검증
        if (recipeDto.getSteps() == null || recipeDto.getSteps().isEmpty() ||
                !ValidateUtils.isValidParam(recipeDto.getTitle()) || !ValidateUtils.isValidParam(recipeDto.getUserID())) {
            throw new CustomException("Title, UserID, 또는 Steps가 누락되었습니다.", HttpStatus.BAD_REQUEST.value());
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
            return new ApiResponse<>(true, "레시피 업로드 성공!", null, HttpStatus.OK);
        } catch (IOException e) {
            throw new CustomException("파일 업로드 실패: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e) {
            throw new CustomException("에러 발생: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @PostMapping("/upload-single")
    public ApiResponse<String> uploadSingleFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ApiResponse<>(false, "File is empty", null, HttpStatus.BAD_REQUEST);
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
            return new ApiResponse<>(true, "File uploaded successfully: " + filePath, null, HttpStatus.OK);
        } catch (IOException e) {
            return new ApiResponse<>(false, "Failed to upload file: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 재료선택 화면 API
    @GetMapping("/findIngredientByFilter")
    public ApiResponse<?> findIngredientByFilter(@RequestParam Map<String, String> params){
        return new ApiResponse<>(true, "성공", foodService.findIngredientByFilter(params), HttpStatus.OK);
    }

}
