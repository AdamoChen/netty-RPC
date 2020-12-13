package com.adamo.controller;

import com.adamo.dto.Student;
import com.adamo.service.StudentService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/student")
public class StudentController {

    @Value("${remote.service.package.scanner}")
    private String remoteServicePackage;

    @Autowired
    StudentService studentService;

    @PostMapping("/add")
    public String add(@RequestBody Student student){
        boolean result = studentService.addStudent(student);
        return JSONObject.toJSONString(result);
    }

    @PostMapping("/all")
    public String add(){
        List<Student> list = studentService.findAllStudent();
        return JSONObject.toJSONString(list);
    }
}
