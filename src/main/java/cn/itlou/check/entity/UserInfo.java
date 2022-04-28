package cn.itlou.check.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class UserInfo {

    @Id
    private String checkName;
    private String token;
    private String emailAddress;

}
