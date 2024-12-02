package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class LambdaHandler  {

    public String handleRequest(Map<String, Object> event, Context context) {
        LambdaLogger logger = context.getLogger();

        try {
            // Log the incoming event
            ObjectMapper objectMapper = new ObjectMapper();
            String eventJson = objectMapper.writeValueAsString(event);
            logger.log("Event Data: " + eventJson);
        } catch (Exception e) {
            logger.log("Error while logging event: " + e.getMessage());
        }


        ScanEmployees scanEmployees = new ScanEmployees();
        Boolean ans =  scanEmployees.sendEmployeMessage();
        if (ans){
            logger.log("Messages sent: " + ans);
        }

        return "Execution completed.";
    }

}

//public class LambdaHandler implements RequestHandler<String,String> {
//
//    @Override
//    public String handleRequest(String input, Context context)
//    {
//        context.getLogger().log("welcome to my first" + input);
//        return "welcome to my first" + input;
//    }
//}

