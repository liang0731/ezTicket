package com.ezticket.web.product.repository;

import com.ezticket.web.product.pojo.Porder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PorderRepository extends JpaRepository<Porder, Integer> {

    @Query("FROM Porder WHERE memberno = :id OR porderno = :id")
    List<Porder> findByID(@Param("id") Integer id);
    Porder findByPaymenttransactionid(String paymenttransactionid);
    Porder findByPorderno(Integer id);
}

