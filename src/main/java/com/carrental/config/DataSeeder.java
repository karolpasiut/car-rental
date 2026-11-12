package com.carrental.config;

import com.carrental.model.Car;
import com.carrental.model.CarType;
import com.carrental.repository.CarRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CarRepository carRepository;

    public DataSeeder(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    @Override
    public void run(String... args) {
        if (carRepository.count() > 0) {
            return;
        }

        List<Car> fleet = List.of(
                car("11111111-1111-1111-1111-111111111101", CarType.SUV, "Toyota", "RAV4",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/0/00/Toyota_RAV4_Core_Plug-in_Hybrid_%28XA60%29_Auto_Zuerich_2025_DSC_3038.jpg/500px-Toyota_RAV4_Core_Plug-in_Hybrid_%28XA60%29_Auto_Zuerich_2025_DSC_3038.jpg?utm_source=pl.wikipedia.org&utm_campaign=parser&utm_content=thumbnail"),
                car("11111111-1111-1111-1111-111111111102", CarType.SUV, "Kia", "Sportage",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e2/Kia_Sportage_Hybrid_Gravity_NQ5_HEV_white_%281%29.jpg/500px-Kia_Sportage_Hybrid_Gravity_NQ5_HEV_white_%281%29.jpg?utm_source=pl.wikipedia.org&utm_campaign=parser&utm_content=thumbnail"),
                car("11111111-1111-1111-1111-111111111103", CarType.SUV, "Hyundai", "Tucson",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c6/2022_Hyundai_Tucson_Preferred%2C_Front_Right%2C_05-24-2021.jpg/500px-2022_Hyundai_Tucson_Preferred%2C_Front_Right%2C_05-24-2021.jpg?utm_source=pl.wikipedia.org&utm_campaign=parser&utm_content=thumbnail"),
                car("11111111-1111-1111-1111-111111111201", CarType.VAN, "Volkswagen", "Sharan",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/a/aa/VW_Sharan_II_2.0_TDI_Reflexsilber.JPG/500px-VW_Sharan_II_2.0_TDI_Reflexsilber.JPG?utm_source=pl.wikipedia.org&utm_campaign=parser&utm_content=thumbnail"),
                car("11111111-1111-1111-1111-111111111202", CarType.VAN, "Ford", "S-Max",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/a/ae/Ford_S-Max_Titanium_%28II%29_%E2%80%93_Frontansicht%2C_8._November_2015%2C_D%C3%BCsseldorf.jpg/500px-Ford_S-Max_Titanium_%28II%29_%E2%80%93_Frontansicht%2C_8._November_2015%2C_D%C3%BCsseldorf.jpg?utm_source=pl.wikipedia.org&utm_campaign=parser&utm_content=thumbnail"),
                car("11111111-1111-1111-1111-111111111203", CarType.VAN, "Chrysler", "Pacifica",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/1/18/2027_Chrysler_Pacifica_Limited_AWD_in_Bright_White%2C_front_left%2C_2026-07-01.jpg/500px-2027_Chrysler_Pacifica_Limited_AWD_in_Bright_White%2C_front_left%2C_2026-07-01.jpg?utm_source=pl.wikipedia.org&utm_campaign=parser&utm_content=thumbnail"),
                car("11111111-1111-1111-1111-111111111301", CarType.SEDAN, "Toyota", "Corolla",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f6/Toyota_Corolla_Limousine_Hybrid_Genf_2019_1Y7A5576.jpg/1280px-Toyota_Corolla_Limousine_Hybrid_Genf_2019_1Y7A5576.jpg?utm_source=pl.wikipedia.org&utm_campaign=imageinfo&utm_content=thumbnail"),
                car("11111111-1111-1111-1111-111111111302", CarType.SEDAN, "Skoda", "Octavia",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9b/Skoda_Octavia_IV_liftback.jpg/500px-Skoda_Octavia_IV_liftback.jpg?utm_source=pl.wikipedia.org&utm_campaign=parser&utm_content=thumbnail"),
                car("11111111-1111-1111-1111-111111111303", CarType.SEDAN, "Mazda", "6",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/4/47/Mazda_6_2.2d_R_2015.jpg/500px-Mazda_6_2.2d_R_2015.jpg?utm_source=pl.wikipedia.org&utm_campaign=parser&utm_content=thumbnail")
        );

        carRepository.saveAll(fleet);
        log.info("Seeded {} sample cars", fleet.size());
    }

    private static Car car(String id, CarType type, String brand, String model, String photoUrl) {
        return new Car(UUID.fromString(id), type, brand, model, photoUrl);
    }
}
