# Lombok使用示例

## 重要提示
  - Spring Data(包含Spring Data MongoDB)默认情况下会使用无参构造函数构建对象，如果没有无参构造函数将采用全参构造函数，因此需要落库的类要么提供无参构造函数，要么提供全参构造函数；Spring官方建议是尽量还是用全参构造函数，但是由于我们的聚合根使用了继承链，因此不能很好地使用全参构造函数，所以聚合根都使用的是无参构造函数；
  - `@Builder`默认情况下将生成`PACKAGE`级别的全参构造函数，需要将其置为`PRIVATE`，以防止程序的错误调用；
  - 需要JSON反序列化的类（比如Command和Query类等），需要提供私有的全参构造函数，即`@AllArgsConstructor(access = PRIVATE)`，以保证Jackson能够使用该全参构造函数构建对象；
  - 由于有`lombok.config`，Lombok将为全参构造函数生成对应的Annotation让Jackson感知到进而调用该全参构造函数；

## Command对象
  - Command对象由于有时重写了Command.correctAndValidate()接口而导致出现更新的情况，因此不是值对象
  - `@Builder`: 用于测试时构造数据
  - `@AllArgsConstructor(access = PRIVATE)`：对外隐藏由`@Builder`自动引入的全参构造函数
   ```
   @Getter
   @Builder
   @AllArgsConstructor(access = PRIVATE)
   public class CreateAppCommand implements Command {}
   ```

## 聚合根
  - 聚合根对象必须通过具有业务含义的自定义构造函数构建，而不能通过`@Builder`任意构建
  - `@NoArgsConstructor(access = PRIVATE)`：Spring MongoDB从数据库重建时将调用该无参构造函数
  - 由于使用了无参构造函数，类中的个字段不能在声明的时候初始化，因为这样重建时将先初始化，从而造成浪费
  ```
  @Getter
  @Document(APP_COLLECTION)
  @TypeAlias("APP")
  @NoArgsConstructor(access = PRIVATE)
  public class App extends AggregateRoot {}
  ```

## 聚合根下的普通对象（包括子实体和内封装对象）
  - `@Builder`保证可以通过Builder创建
  - `@AllArgsConstructor(access = PRIVATE)`：保证Spring MongoDB可以通过全参构造函数重建，并且对外隐藏由`@Builder`自动引入的全参构造函数
  - 实体是可以改变的，因此不能标记@Value
  - 创建聚合根时需要在自定义构造函数中调用这些builder
  - 对于有唯一标识的对象，需要集成自`Entity`
  
  ```
  @Getter
  @Builder
  @AllArgsConstructor(access = PRIVATE)
  public class Page implements Entity {}
  ```
  或者
  ```
    @Getter
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    public class ApiSetting {}
  ```

## 普通值对象
  - 值对象是不变的，因此需要标记@Value
  - `@Builder`：值对象需要一次性创建完成，使用builder会比直接调用全参构造函数好用些，另外保证Spring MongoDB可以通过全参构造函数重建，并且对外隐藏由`@Builder`自动引入的全参构造函数
  - `@AllArgsConstructor(access = PRIVATE)`：对外隐藏由`@Builder`自动引入的全参构造函数
  
  ```
  @Value
  @Builder
  @AllArgsConstructor(access = PRIVATE)
  public class Plan {}
  ```
  
## 需要比较相等，但是又可以修改的对象
- `@Builder`：用于测试时通过builder构建
- `@EqualsAndHashCode`：生成equals方法，由于需要修改，因此不能使用`@Value`

```
@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class AppConfig {}
```

## 领域事件
  - 领域事件通过自定义构造函数构建，因此不需要`@Builder`
  - `@NoArgsConstructor(access = PRIVATE)`:保证Spring MongoDB可以通过无参构造函数从数据库重建对象
  
  ```
  @Getter
  @TypeAlias("APP_CREATED_EVENT")
  @NoArgsConstructor(access = PRIVATE)
  public class AppCreatedEvent extends DomainEvent {}
  ```

## Query返回对象
  - Query返回对象是一种值对象，因此Lombok标记和值对象相同
   ```
   @Value
   @Builder
   @AllArgsConstructor(access = PRIVATE)
   public class QAboutInfo {}
   ```
