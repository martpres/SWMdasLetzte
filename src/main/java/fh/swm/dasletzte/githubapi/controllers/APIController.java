package fh.swm.dasletzte.githubapi.controllers;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fh.swm.dasletzte.githubapi.models.response.ApiResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
public class APIController {
    /*
    Test variable for checking different if statements
    -----
    String responseTextGithub = "repoExists";
    String responseTextGithub = "repoTimeout";
    String responseTextGithub = "repoSpecialCharacter";
    String responseTextGithub = "repoInvalidToken";
    String responseTextGithub = "repoMissingToken";
    String responseTextGithub = "repoMissingName";
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(3000))
                .setReadTimeout(Duration.ofMillis(3000))
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
        if (token == null) {
            apiResponse.setMessage("missing api token");
            return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
        }

        System.out.println(token);
        System.out.println(repoName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("authorization", token);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBodyMap, headers);

        try {
            RestTemplate restTemplate = new RestTemplate();
            // it could be, that the token has expired -> then we receive a 500 internal server error?
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                    "https://api.github.com/user/repos", entity, String.class);
            statusCode = responseEntity.getStatusCodeValue();
            Map responseBodyMap = objectMapper.readValue(responseEntity.getBody(), Map.class);
            htmlUrl = responseBodyMap.get("html_url").toString();
        } catch (HttpClientErrorException ex) {
            statusCode = ex.getRawStatusCode();
            Map<String, Object> bodyErrorMap = objectMapper.readValue(ex.getResponseBodyAsString(), Map.class);
            JSONObject json = new JSONObject(ex.getResponseBodyAsString());
            JSONArray jsonArray = json.getJSONArray("errors");
            JSONObject item = jsonArray.getJSONObject(0);
            statusErrorMessage = item.get("message").toString();
            System.out.println(item.get("message"));
        }
        System.out.println("Status: " + statusCode);

        // E7 SUCCESS
        if (statusCode == 201 || statusCode == 200) {
            apiResponse.setRepoUrl(htmlUrl);
            return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
            // E1 REPO ALREADY EXISTS
        } else if (statusCode == 422 && statusErrorMessage.equals("name already exists on this account")) {
            apiResponse.setMessage(statusErrorMessage);
            return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);
        }

        // E3 and general error since special characters will be process by GitHub
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }


    /**
     * @GetMapping("/apiv2") public ResponseEntity<ApiResponse> createRepository(@RequestParam(value = "userId") String userId) {
     * ApiResponse api = new ApiResponse();
     * int statusCode = 0;
     * <p>
     * try {
     * ResponseEntity<String> responseEntity = restTemplate.getForEntity(
     * "https://api.github.com/users/" + userId, String.class);
     * statusCode = responseEntity.getStatusCodeValue();
     * } catch (HttpClientErrorException ex) {
     * statusCode = ex.getRawStatusCode();
     * }
     * if (statusCode == 200) {
     * api.setMessage("OK");
     * return new ResponseEntity<>(api, HttpStatus.OK);
     * } else {
     * api.setMessage("NOK");
     * return new ResponseEntity<>(api, HttpStatus.BAD_REQUEST);
     * }
     * //System.out.println("Request for user: " + userId);
     * //System.out.println(responseEntity.getBody());
     * <p>
     * }
     **/

    @GetMapping("/api")
    public ResponseEntity<String> getRepository(@RequestParam(value = "message") String message) {
        if (message == null || message.equals("")) {
            return new ResponseEntity<>("No repository name entered", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }



    /*
    @PostMapping("/api")
    public ResponseEntity<ApiResponse> createRepository(@RequestParam(value = "repo") String repo) {
        ApiResponse api = new ApiResponse();
        // E1 repo already exists
        if (responseTextGithub.equals("repoExists")) {
            api.setMessage("CONFLICT");
            return new ResponseEntity<>(api, HttpStatus.CONFLICT);
        }
        // E2 timeout accessing GitHub
        else if (responseTextGithub.equals("repoTimeout")) {
            api.setMessage("GATEWAY_TIMEOUT");
            return new ResponseEntity<>(HttpStatus.GATEWAY_TIMEOUT);
        }
        // E3 invalid special characters in repo name
        else if (responseTextGithub.equals("repoSpecialCharacter")) {
            api.setMessage("BAD_REQUEST");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // E4 API token invalid
        else if (responseTextGithub.equals("repoInvalidToken")) {
            api.setMessage("UNAUTHORIZED");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        // E5 API token not in header
        else if (responseTextGithub.equals("repoMissingToken")) {
            api.setMessage("UNAUTHORIZED");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        // E6 repo name not in body / empty string for repo name (only spaces, tabs, newline, …)
        else if (responseTextGithub.equals("repoMissingName")) {
            api.setMessage("BAD_REQUEST");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // E7 success
        api.setMessage("OK");
        api.setName(repo);
        api.setHtml_url("https://github.com/<username>/" + repo);
        return new ResponseEntity<>(api, HttpStatus.OK);
    }
    */
}
