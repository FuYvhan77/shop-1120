/**
 * @author zhangyadong
 * @version 1.0
 * @ClassName Demo
 * @date 2026-05-29 8:59
 */
// 产品接口
//interface Phone {
//    void call();
//}
//
//// 具体产品
//class iPhone implements Phone {
//    public void call() { System.out.println("用 iPhone 打电话"); }
//}
//class Samsung implements Phone {
//    public void call() { System.out.println("用三星打电话"); }
//}
//
//// 简单工厂（静态工厂）
//class PhoneFactory {
//    public static Phone createPhone(String brand) {
//        if ("iPhone"==brand) {
//            return new iPhone();
//        } else if ("Samsung"==brand) {
//            return new Samsung();
//        } else {
//            throw new IllegalArgumentException("不支持的品牌: " + brand);
//        }
//    }
//}


// 产品接口不变
interface Car {
    void drive();
}

// 具体产品
class Benz implements Car {
    public void drive() { System.out.println("驾驶奔驰，豪华舒适"); }
}
class BMW implements Car {
    public void drive() { System.out.println("驾驶宝马，操控为王"); }
}

// 抽象工厂
interface CarFactory {
    Car createCar();
}

// 每个具体产品对应一个具体工厂
class BenzFactory implements CarFactory {
    public Car createCar() { return new Benz(); }
}
class BMWFactory implements CarFactory {
    public Car createCar() { return new BMW(); }
}

class Demo {
    public static void main(String[] args) {
        CarFactory factory = new BenzFactory();
        Car car = factory.createCar();

    }
}
