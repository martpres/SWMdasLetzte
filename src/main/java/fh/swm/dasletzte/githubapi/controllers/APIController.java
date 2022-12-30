package fh.swm.dasletzte.githubapi.controllers;

import fh.swm.dasletzte.githubapi.models.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    String responseTextGithub = "OK";

    @GetMapping("/api")
    public ResponseEntity<String> getRepository(@RequestParam(value = "message") String message) {
        if (message == null || message.equals("")) {
            return new ResponseEntity<>("No repository name entered", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * This method is responsible for various repository operations
     *
     * @param repo repository name
     * @return response via http
     */
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
}
