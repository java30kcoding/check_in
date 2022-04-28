package cn.itlou.check.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
public class CheckConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String host;
    private String checkUrl;
    private int retryCount;

}
