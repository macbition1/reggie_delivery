package com.chun.reggie.controller;

import com.chun.reggie.common.Result;
import com.sun.deploy.net.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;


/**
 * File upload and download
 */

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${uploadFile.path}")
    private String basePath;


    /**
     * Upload picture of food, here is image
     * @param file
     * @return
     */
    @PostMapping("/upload")
    //MultipartFile variable must same as type of front page
    public Result<String> upload(MultipartFile file){

        //file is a temporary file
        log.info(file.toString());

        String originalFileName = file.getOriginalFilename(); //abc.jpg

        String suffix = originalFileName.substring(originalFileName.lastIndexOf("."));


        //create filename to avoid original file duplicated name
         String fileName = UUID.randomUUID().toString() + suffix;

         //check File exist
        File dir = new File(basePath);
        if(!dir.exists()){
            //not exists, create a new file path
            dir.mkdirs();
        }

        //save temporary file at a place assigned
        try {
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Result.success(fileName);
    }


    /**
     * Download file, here is image
     * @param name
     * @param response
     */

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        //read file from input stream
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));
            //display file to the page from output stream
             ServletOutputStream outputStream = response.getOutputStream();

             response.setContentType("image/jpeg");

             //write to page
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1){
                outputStream.write(bytes, 0,len);
                outputStream.flush();
            }

            //release resource
            outputStream.close();
            fileInputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }




    }

}
