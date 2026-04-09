//package com.recipeapp.recipe_service.Feign;
//
//import com.recipeapp.recipe_service.DTO.UserDto;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//
//@FeignClient(name = "USER-SERVICE", url = "http://localhost:8081")
//public interface UserClient {
//
//    @GetMapping("/users/{id}")   // ✅ MATCHED PATH
//    UserDto getUserById(@PathVariable long id);
//}
