package fh.swm.dasletzte.githubapi.controllers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import fh.swm.dasletzte.githubapi.models.response.ApiResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible for the interaction between the REST API and GitHub API
 */
@RestController
public class APIController {
    /**
     * This method is repsonsible for providing the template for the communication
     * between our API and GitHub API
     * @param builder
     * @return template to use
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(0))
                .setReadTimeout(Duration.ofMillis(0))
                .build();
    }

    /**
     * This method is responsible for various repository operations
     *
     * @param paramHeaders authorization header with Bearer token
     * @param postBody     json-formatted string with repo name
     * @return response via http
     */
    @PostMapping("/apiv2")
    public ResponseEntity<ApiResponse> createRepository(
            @RequestHeader Map<String, String> paramHeaders,
            @RequestBody Map<String, String> postBody) throws JsonProcessingException, JSONException {

        ObjectMapper objectMapper = new ObjectMapper();
        ApiResponse apiResponse = new ApiResponse();
        int statusCode = 0;
        String statusErrorMessage = "";
        String htmlUrl = "";
        String token = paramHeaders.get("authorization");
        String repoName = "";

        repoName = postBody.get("repoName");

        Map<String, String> requestBodyMap = new HashMap<>();
        requestBodyMap.put("name", repoName);

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("repoName", repoName);

        // E5 TOKEN MISSING
        if (token == null || token.isEmpty()) {
            apiResponse.setMessage("missing api token");
            return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
        }
        // E6 REPO NAME NOT IN BODY
        if (repoName == null || repoName.isEmpty()) {
            apiResponse.setMessage("repo name not in body");
            return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("authorization", token);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBodyMap, headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                    "https://api.github.com/user/repos", entity, String.class);
            statusCode = responseEntity.getStatusCodeValue();
            Map responseBodyMap = objectMapper.readValue(responseEntity.getBody(), Map.class);
            htmlUrl = responseBodyMap.get("html_url").toString();
        } catch (HttpClientErrorException hcEx) {
            statusCode = hcEx.getRawStatusCode();
            try {
                Map bodyErrorMap = objectMapper.readValue(hcEx.getResponseBodyAsString(), Map.class);
                JSONObject json = new JSONObject(hcEx.getResponseBodyAsString());
                JSONArray jsonArray = json.getJSONArray("errors");
                JSONObject item = jsonArray.getJSONObject(0);
                statusErrorMessage = item.get("message").toString();
                System.out.println(item.get("message"));
            } catch (MismatchedInputException miEx) {
                statusErrorMessage = "invalid token";
                statusCode = 500;
            }
        } catch (ResourceAccessException e) {
            statusErrorMessage = "timeout access to github.com";
            statusCode = 504;
        }
        // E7 SUCCESS
        if (statusCode == 201 || statusCode == 200) {
            apiResponse.setRepoUrl(htmlUrl);
            return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
            // E1 REPO ALREADY EXISTS
        } else if (statusCode == 422 && statusErrorMessage.equals("name already exists on this account")) {
            apiResponse.setMessage(statusErrorMessage);
            return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);
            // E4 INVALID OR EXPIRED TOKEN
        } else if (statusCode == 500 && statusErrorMessage.equals("invalid token")) {
            apiResponse.setMessage(statusErrorMessage);
            return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
            // E2 TIMEOUT ACCESS TO GITHUB.COM
        } else if (statusCode == 504 && statusErrorMessage.equals("timeout access to github.com")) {
            apiResponse.setMessage(statusErrorMessage);
            return new ResponseEntity<>(apiResponse, HttpStatus.GATEWAY_TIMEOUT);
        }
        // E3 and general error since special characters will be process by GitHub
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
