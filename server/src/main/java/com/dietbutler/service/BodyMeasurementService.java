package com.dietbutler.service;

import com.dietbutler.entity.BodyMeasurement;
import com.dietbutler.repository.BodyMeasurementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BodyMeasurementService {

    private final BodyMeasurementRepository bodyMeasurementRepository;

    public BodyMeasurement addMeasurement(Long userId, Double waist, Double hip, Double chest,
                                          Double upperArm, Double forearm, Double thigh, Double calf,
                                          String note, LocalDate recordDate) {
        LocalDate targetDate = recordDate != null ? recordDate : LocalDate.now();
        BodyMeasurement m = new BodyMeasurement();
        m.setUserId(userId);
        m.setWaist(waist);
        m.setHip(hip);
        m.setChest(chest);
        m.setUpperArm(upperArm);
        m.setForearm(forearm);
        m.setThigh(thigh);
        m.setCalf(calf);
        m.setRecordDate(targetDate);
        return bodyMeasurementRepository.save(m);
    }

    public List<BodyMeasurement> getHistory(Long userId) {
        return bodyMeasurementRepository.findByUserIdOrderByRecordDateDesc(userId);
    }

    public List<BodyMeasurement> getHistory(Long userId, int limit) {
        return bodyMeasurementRepository.findByUserIdOrderByRecordDateDesc(userId).stream().limit(limit).toList();
    }

    public List<BodyMeasurement> getRecent(Long userId, int days) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days);
        return bodyMeasurementRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(userId, start, end);
    }

    public Optional<BodyMeasurement> getLatest(Long userId) {
        List<BodyMeasurement> records = bodyMeasurementRepository.findByUserIdOrderByRecordDateDesc(userId);
        return records.isEmpty() ? Optional.empty() : Optional.of(records.get(0));
    }

    public BodyMeasurement updateMeasurement(Long id, Double waist, Double hip, Double chest,
                                              Double upperArm, Double forearm, Double thigh, Double calf,
                                              LocalDate recordDate) {
        Optional<BodyMeasurement> opt = bodyMeasurementRepository.findById(id);
        if (opt.isEmpty()) return null;
        BodyMeasurement m = opt.get();
        if (waist != null) m.setWaist(waist);
        if (hip != null) m.setHip(hip);
        if (chest != null) m.setChest(chest);
        if (upperArm != null) m.setUpperArm(upperArm);
        if (forearm != null) m.setForearm(forearm);
        if (thigh != null) m.setThigh(thigh);
        if (calf != null) m.setCalf(calf);
        if (recordDate != null) m.setRecordDate(recordDate);
        return bodyMeasurementRepository.save(m);
    }

    public void deleteMeasurement(Long id) {
        bodyMeasurementRepository.deleteById(id);
    }
}
