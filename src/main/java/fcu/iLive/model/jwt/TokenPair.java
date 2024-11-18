package fcu.iLive.model.jwt;

public class TokenPair {
  private String accessToken;
  private String refreshToken;

  public TokenPair(String accessToken, String refreshToken) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
  }

  // getters and setters
}
