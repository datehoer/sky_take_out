package com.sky.controller.admin;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Request;
import okhttp3.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Slf4j
@Api(tags = "通用接口")
public class CommonController {
    private final AliOssUtil aliOssUtil;

    public CommonController(AliOssUtil aliOssUtil) {
        this.aliOssUtil = aliOssUtil;
    }

    @PostMapping("/upload")
    @ApiOperation("上传图片")
    public Result<String> upload(MultipartFile file){
        log.info("上传图片：{}", file.getOriginalFilename());
        try{
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null) {
                String substring = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uuid = UUID.randomUUID() + substring;
//                String upload = aliOssUtil.upload(multipartFile.getBytes(), uuid);
//                return Result.success(upload);
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
                .addHeader("Authorization", "Basic F1eK6ESsMUIrBOT6GGKHISfNOvN48uYk")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
                .post(requestBody)
                .build();
        log.info("上传请求：{}", request);
        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        log.info("响应代码：{}", response.code());
        log.info("响应消息：{}", responseBody);
        if (response.isSuccessful()) {
            JSONObject jsonObject = JSONObject.parseObject(responseBody);
            if (jsonObject.getBoolean("success")) {
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