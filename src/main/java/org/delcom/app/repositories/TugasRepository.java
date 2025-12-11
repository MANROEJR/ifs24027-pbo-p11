package org.delcom.app.repositories;

import java.util.List;
import java.util.UUID;

import org.delcom.app.entities.Tugas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TugasRepository extends JpaRepository<Tugas, UUID> {

    // 1. Mengambil semua tugas milik user tertentu
    // 2. Diurutkan berdasarkan DEADLINE (dari yang terdekat/terlama) -> Ascending
    List<Tugas> findAllByUserIdOrderByDeadlineAsc(UUID userId);

    // Opsional: Jika nanti ingin memfilter tugas yang "Belum Selesai" saja
    List<Tugas> findAllByUserIdAndStatus(UUID userId, String status);
    
}