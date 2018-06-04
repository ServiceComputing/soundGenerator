package com.classcourse.soundGenerator.Controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.classcourse.soundGenerator.WaveAccess.WaveFileReader;
import com.classcourse.soundGenerator.WaveAnalyze.*;

@RestController
public class SoundGeneratorController {
    static final int RATE_HZ = 48000;
    static final int BUFFER_SIZE = RATE_HZ;

    /**
     * variables for calculation
     */
    private short[] pnData; //the PN sequence data
    private short[] rawSound; //raw sound data
    private double[] x;
    private double[] y; //location of speakers
    private double[] dd;
    private LocationCalculator locationCalculator;
    private CrossCorrelation correlation;
    private CorrelationAnalyzer analyzer;

    SoundGeneratorController() {
        //init
        rawSound = new short[BUFFER_SIZE];
        locationCalculator = new LocationCalculator(3);
        analyzer = new CorrelationAnalyzer(3);
        correlation = new CrossCorrelation(BUFFER_SIZE);
        x = new double[3];
        y = new double[3];
        dd = new double[2];

        //location of 3 speakers
        x[0] = 0;
        x[1] = 0;
        x[2] = 1;
        y[0] = 1;
        y[1] = 0;
        y[2] = 0;
        locationCalculator.updatePosition(x, y);

        //get raw PN data
        try {
            InputStream in = this.getClass().getResourceAsStream("/data48000_18_20_200ms");
            int length = in.available();
            byte[] tmp = new byte[length];
            //noinspection ResultOfMethodCallIgnored
            in.read(tmp);
            in.close();
            pnData = new short[length / 2];
            for (int i = 0; i < pnData.length; ++i) {
                int tmp1 = tmp[2 * i] << 8;
                int tmp2 = tmp[2 * i + 1] & 0x000000ff;
                pnData[i] = (short) (tmp1 + tmp2);
            }
            System.out.println("Read raw data!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    @RequestMapping(value = "/testAnalyze", method = RequestMethod.GET)
    public String testAnalyzeSound () {
        String result = "Success!";
        WaveFileReader reader = new WaveFileReader("/Users/cebiaozhu/Downloads/record.wav");
        int[][] data = reader.getData();

        for (int i = 0; i < rawSound.length; ++i) {
            if (i < data[0].length) {
                rawSound[i] = (short) data[0][i];
            } else {
                rawSound[i] = 0;
            }
        }

        double[] correlationData;
        int[] resultIndex;
        double[] locationResult;//location result
        correlationData = correlation.getResult(rawSound, pnData);
        resultIndex = analyzer.cal(correlationData);

        dd[0] = -((double) (resultIndex[1] - resultIndex[0]) / RATE_HZ - 0.25) * 340;
        dd[1] = -((double) (resultIndex[2] - resultIndex[1]) / RATE_HZ - 0.25) * 340;
        locationResult = locationCalculator.cal(dd);
        System.out.println(locationResult[0]);//x
        System.out.println(locationResult[1]);//y
        return result;
    }

}
