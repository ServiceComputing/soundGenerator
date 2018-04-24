package com.classcourse.soundGenerator.Controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

@RestController
public class SoundGeneratorController {

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String generateSound () {
        return "Success!";
    }

    @RequestMapping(value = "/uploadSound", method = RequestMethod.POST)
    public String handleUploadFiles (HttpServletRequest request) {
        System.out.println("Start Uploading...");
        MultipartHttpServletRequest params = ((MultipartHttpServletRequest) request);
        List<MultipartFile> files = params.getFiles("file");
        ArrayList<String> names = new ArrayList<String>();
        MultipartFile file = null;
        BufferedOutputStream stream = null;
        for (int i = 0; i < files.size(); ++i) {
            System.out.println("File Index = " + i);
            file = files.get(i);
            if (!file.isEmpty()) {
                try {
                    byte[] bytes = file.getBytes();
                    stream = new BufferedOutputStream(new FileOutputStream(new File(file.getOriginalFilename())));
                    stream.write(bytes);
                    stream.close();
                    names.add(file.getOriginalFilename());
                } catch (Exception e) {
                    e.printStackTrace();
                    stream = null;
                    return "You failed to Upload " + file.getOriginalFilename() + " => " + e.getMessage();
                }
            } else {
                return "File " + i + " is empty. Upload Failed!";
            }
        }
        String returnStr = "[";
        for (int i = 0; i < names.size(); ++i) {
            returnStr += names.get(i);
        }
        returnStr += "]";
        System.out.println("Success Uploading " + returnStr + "!");
        return "Success Uploading " + returnStr + "!";
    }


}
