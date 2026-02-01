package com.aeon.acss.fdu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aeon.acss.fdu.model.ClientModel;

@Repository
public interface ClientRepository extends JpaRepository<ClientModel, Integer> {

}
