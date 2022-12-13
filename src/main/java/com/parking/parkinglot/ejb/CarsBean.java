package com.parking.parkinglot.ejb;

import com.parking.parkinglot.common.CarDto;
import com.parking.parkinglot.entities.Car;
import com.parking.parkinglot.entities.User;
import jakarta.ejb.EJBException;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

@Stateless
public class CarsBean {

    private static final Logger LOG = Logger.getLogger(CarsBean.class.getName());

    @PersistenceContext
    EntityManager entityManager;

    public List<CarDto> findAllCars() {
        LOG.info("findAllCars");
        try {
            TypedQuery<Car> typedQuery =
                    entityManager.createQuery("SELECT c FROM Car c", Car.class);
            List<Car> cars = typedQuery.getResultList();
            return copyCarsToDto(cars);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    public List<CarDto> copyCarsToDto(List<Car> cars) {
        List<CarDto> carsDto = new LinkedList<CarDto>();

        for (int i = 0; i < cars.size(); i++) {
            CarDto dto = new CarDto(cars.get(i).getId(),
                    cars.get(i).getLicensePlate(),
                    cars.get(i).getParkingSpot(),
                    cars.get(i).getOwner().getUsername());
            carsDto.add(dto);
        }

        return carsDto;
    }

    public void createCar(
            String licensePlate,
            String parkingSpot,
            Long userId
    ) {
        LOG.info("createCar");

        Car car = new Car();

        User user = entityManager.find(User.class, userId);
        user.getCars().add(car);

        car.setLicensePlate(licensePlate);
        car.setParkingSpot(parkingSpot);
        car.setOwner(user);

        entityManager.persist(car);
    }

    public CarDto findById(Long carId) {
        CarDto car = null;

        List<CarDto> allCars = findAllCars();

        for (int i = 0; i < allCars.size(); i++) {
            if (Objects.equals(allCars.get(i).getId(), carId)) {
                car = allCars.get(i);
            }
        }

        return car;
    }

    public void updateCar(
        Long carId,
        String licensePlate,
        String parkingSpot,
        Long userId
    ) {
        LOG.info("updateCar");

        Car car = entityManager.find(Car.class, carId);
        car.setLicensePlate(licensePlate);
        car.setParkingSpot(parkingSpot);

        User oldUser = car.getOwner();
        oldUser.getCars().remove(car);

        User user = entityManager.find(User.class, userId);
        user.getCars().add(car);
        car.setOwner(user);
    }

    public void deleteCarsByIds(Collection<Long> carIds) {
        LOG.info("deleteCarsByIds");

        for (Long carId : carIds) {
            Car car = entityManager.find(Car.class, carId);
            entityManager.remove(car);
        }
    }
}
