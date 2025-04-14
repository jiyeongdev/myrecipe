//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.sdemo1.controller;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.common.utils.ValidateUtils;
import com.sdemo1.dto.RecipeDto;
import com.sdemo1.dto.RecipeStepDto;
import com.sdemo1.exception.CustomException;

import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@RestController
@RequestMapping({"/file"})
public class FileUploadController {
    @Value("${aws.s3.bucket}")
    private String bucketName;
    @Value("${aws.s3.region}")
    private String region;
    @Value("${aws.s3.recipe-folder}")
    private String recipeFolder;
    private S3Client s3Client;

    public FileUploadController() {
    }

    @PostConstruct
    public void initializeS3Client() {
        this.s3Client = (S3Client)((S3ClientBuilder)((S3ClientBuilder)S3Client.builder().region(Region.of(this.region))).credentialsProvider(ProfileCredentialsProvider.create())).build();
    }

    @PostMapping({"/upload-recipe"})
    public ApiResponse<String> uploadRecipe(@RequestPart(value = "recipe",required = false) RecipeDto recipeDto, @RequestParam("files") List<MultipartFile> files) {
        System.out.println("=====================");
        System.out.println(recipeDto);
        if (recipeDto.getSteps() != null && !recipeDto.getSteps().isEmpty() && ValidateUtils.isValidParam(recipeDto.getTitle()) && ValidateUtils.isValidParam(recipeDto.getUserID())) {
            try {
                List<String> uploadedUrls = new ArrayList();

                for(int i = 0; i < recipeDto.getSteps().size(); ++i) {
                    RecipeStepDto var10000 = (RecipeStepDto)recipeDto.getSteps().get(i);
                    MultipartFile file = (MultipartFile)files.get(i);
                    UUID var11 = UUID.randomUUID();
                    String fileName = var11 + "-" + file.getOriginalFilename();
                    this.s3Client.putObject((PutObjectRequest)PutObjectRequest.builder().bucket(this.bucketName).key(fileName).contentType(file.getContentType()).build(), RequestBody.fromBytes(file.getBytes()));
                    String fileUrl = "https://" + this.bucketName + ".s3." + this.region + ".amazonaws.com/" + fileName;
                    uploadedUrls.add(fileUrl);
                    System.out.println("fileURL : " + fileUrl);
                }

                return new ApiResponse(true, "레시피 업로드 성공!", (Object)null, HttpStatus.OK);
            } catch (IOException var9) {
                throw new CustomException("파일 업로드 실패: " + var9.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
            } catch (Exception var10) {
                throw new CustomException("에러 발생: " + var10.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        } else {
            throw new CustomException("Title, UserID, 또는 Steps가 누락되었습니다.", HttpStatus.BAD_REQUEST.value());
        }
    }

    @PostMapping({"/upload-single"})
    public ApiResponse<String> uploadSingleFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ApiResponse(false, "File is empty", (Object)null, HttpStatus.BAD_REQUEST);
        } else {
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            try {
                String filePath = uploadDir + file.getOriginalFilename();
                file.transferTo(new File(filePath));
                return new ApiResponse(true, "File uploaded successfully: " + filePath, (Object)null, HttpStatus.OK);
            } catch (IOException var5) {
                return new ApiResponse(false, "Failed to upload file: " + var5.getMessage(), (Object)null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @PostMapping({"/get-presigned-url"})
    public ApiResponse<Map<String, String>> getPresignedUrl(@RequestParam("userId") Integer userId, @RequestParam("contentType") String contentType) {
        try {

            
            String uniqueFileName = userId.toString();
            String bucketPath = this.recipeFolder + "/" + uniqueFileName;
            S3Presigner presigner = S3Presigner.builder().region(Region.of(this.region)).credentialsProvider(ProfileCredentialsProvider.create()).build();

            ApiResponse var10;
            try {
                PutObjectRequest objectRequest = (PutObjectRequest)PutObjectRequest.builder().bucket(this.bucketName).key(bucketPath).contentType(contentType).build();
                PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder().signatureDuration(Duration.ofMinutes(10L)).putObjectRequest(objectRequest).build();
                PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
                Map<String, String> response = new HashMap();
                response.put("presignedUrl", presignedRequest.url().toString());
                response.put("fileUrl", String.format("https://%s.s3.%s.amazonaws.com/%s", this.bucketName, this.region, bucketPath));
                presigner.close();
                var10 = new ApiResponse(true, "Presigned URL 생성 성공", response, HttpStatus.OK);
            } catch (Throwable var12) {
                if (presigner != null) {
                    try {
                        presigner.close();
                    } catch (Throwable var11) {
                        var12.addSuppressed(var11);
                    }
                }

                throw var12;
            }

            if (presigner != null) {
                presigner.close();
            }

            return var10;
        } catch (Exception var13) {
            throw new CustomException("Presigned URL 생성 실패: " + var13.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @PostMapping({"/get-presigned-urls"})
    public ApiResponse<List<Map<String, String>>> getPresignedUrls(@RequestParam("count") int count, @RequestParam("contentType") String contentType) {
        try {
            List<Map<String, String>> presignedUrls = new ArrayList();
            S3Presigner presigner = S3Presigner.builder().region(Region.of(this.region)).credentialsProvider(ProfileCredentialsProvider.create()).build();

            try {
                for(int i = 0; i < count; ++i) {
                    String extension = this.getExtensionFromContentType(contentType);
                    String var10000 = UUID.randomUUID().toString();
                    String uniqueFileName = var10000 + "-image" + (i + 1) + "." + extension;
                    String bucketPath = this.recipeFolder + "/" + uniqueFileName;
                    PutObjectRequest objectRequest = (PutObjectRequest)PutObjectRequest.builder().bucket(this.bucketName).key(bucketPath).contentType(contentType).build();
                    PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder().signatureDuration(Duration.ofMinutes(10L)).putObjectRequest(objectRequest).build();
                    PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
                    Map<String, String> urlInfo = new HashMap();
                    urlInfo.put("presignedUrl", presignedRequest.url().toString());
                    urlInfo.put("fileUrl", String.format("https://%s.s3.%s.amazonaws.com/%s", this.bucketName, this.region, bucketPath));
                    presignedUrls.add(urlInfo);
                }
            } catch (Throwable var14) {
                if (presigner != null) {
                    try {
                        presigner.close();
                    } catch (Throwable var13) {
                        var14.addSuppressed(var13);
                    }
                }

                throw var14;
            }

            if (presigner != null) {
                presigner.close();
            }

            return new ApiResponse(true, "Presigned URLs 생성 성공", presignedUrls, HttpStatus.OK);
        } catch (Exception var15) {
            throw new CustomException("Presigned URLs 생성 실패: " + var15.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private String getExtensionFromContentType(String contentType) {
        byte var3 = -1;
        switch(contentType.hashCode()) {
        case -1487394660:
            if (contentType.equals("image/jpeg")) {
                var3 = 0;
            }
            break;
        case -1487018032:
            if (contentType.equals("image/webp")) {
                var3 = 3;
            }
            break;
        case -879267568:
            if (contentType.equals("image/gif")) {
                var3 = 2;
            }
            break;
        case -879258763:
            if (contentType.equals("image/png")) {
                var3 = 1;
            }
        }

        String var10000;
        switch(var3) {
        case 0:
            var10000 = "jpg";
            break;
        case 1:
            var10000 = "png";
            break;
        case 2:
            var10000 = "gif";
            break;
        case 3:
            var10000 = "webp";
            break;
        default:
            throw new CustomException("지원하지 않는 파일 형식입니다: " + contentType, HttpStatus.BAD_REQUEST.value());
        }

        return var10000;
    }
}
