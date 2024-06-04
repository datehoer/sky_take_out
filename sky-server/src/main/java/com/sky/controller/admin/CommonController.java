package com.sky.controller.admin;
import com.alibaba.fastjson2.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Request;
import okhttp3.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Objects;

@RestController
@RequestMapping("/admin/common")
@Slf4j
@Api(tags = "通用接口")
public class CommonController {
    @Value("${sky.smms.token}")
    private String smmsToken;

    @PostMapping("/upload")
    @ApiOperation("上传图片")
    public Result<String> upload(MultipartFile file){
        log.info("上传图片：{}", file.getOriginalFilename());
        try{
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null) {
                String smmsUploadUrl = uploadToSmms(file);
                if (smmsUploadUrl != null) {
                    return Result.success(smmsUploadUrl);
                } else {
                    return Result.error(MessageConstant.UPLOAD_FAILED);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
    private String uploadToSmms(MultipartFile multipartFile) throws IOException {
        OkHttpClient client = new OkHttpClient();
        RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), multipartFile.getBytes());
        RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("smfile", multipartFile.getOriginalFilename(), fileBody)
            .build();
        Request request = new Request.Builder()
                .url("https://smms.app/api/v2/upload")
                .addHeader("Authorization", "Basic " + smmsToken)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
                .post(requestBody)
                .build();
        Response response = client.newCall(request).execute();
        String responseBody = null;
        if (response.body() != null) {
            responseBody = response.body().string();
        }
        if (response.isSuccessful()) {
            JSONObject jsonObject = JSONObject.parseObject(responseBody);
            if (jsonObject == null){
                log.error("SMMS上传失败，响应为空");
                return null;
            }
            if (jsonObject.getBooleanValue("success")){
                return jsonObject.getJSONObject("data").getString("url");
            } else if (Objects.equals(jsonObject.getString("code"), "image_repeated")) {
                return jsonObject.getString("images");
            } else {
                log.error("SMMS上传失败：{}", jsonObject.getString("message"));
            }
        } else {
            log.error("SMMS上传失败，响应状态：{}", response.code());
        }
        return null;
    }
}