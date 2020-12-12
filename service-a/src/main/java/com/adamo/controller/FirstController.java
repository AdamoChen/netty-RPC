package com.adamo.controller;

import com.adamo.dto.Student;
import com.adamo.service.StudentService;
import org.chen.proxy.RemoteServiceDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/first")
public class FirstController {

    @Value("${remote.service.package.scanner}")
    private String remoteServicePackage;

    @Autowired
    RemoteServiceDefinitionRegistryPostProcessor a;

    @Autowired
    StudentService studentService;

    @PostMapping("/add")
    public String add(@RequestBody Student student){
        boolean result = studentService.addStudent(student);
        return "success";
    }
}
