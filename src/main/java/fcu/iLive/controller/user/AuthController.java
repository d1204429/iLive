////認證相關 API (/auth/**)
//
//package fcu.iLive.controller.user;
//
//import fcu.iLive.dto.request.auth.LoginRequest;
//import fcu.iLive.dto.request.auth.RegisterRequest;
//import fcu.iLive.dto.response.auth.JwtResponse;
//import fcu.iLive.model.user.User;
//import fcu.iLive.service.user.UserService;
//import fcu.iLive.util.JwtUtil;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/v1/auth")
//public class AuthController {
//  @Autowired
//  private UserService userService;
//
//  @Autowired
//  private JwtUtil jwtUtil;
//
//  @PostMapping("/register")
//  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
//    User user = userService.register(request);
//    return ResponseEntity.ok(new RegisterResponse(user));
//  }
//
//  @PostMapping("/login")
//  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
//    User user = userService.login(request);
//    String token = jwtUtil.generateToken(user);
//    return ResponseEntity.ok(new JwtResponse(token, user));
//  }
//}