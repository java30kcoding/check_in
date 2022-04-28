package cn.itlou.check.repo;

import cn.itlou.check.entity.CheckConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckConfigRepository extends JpaRepository<CheckConfig, Integer> {
}
