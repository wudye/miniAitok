DTO, VO
@EaqualsAndHashCode(callSuper = false)
value method in a Enum class
create a baseDO with createdTime , updatedTime, createBy, updateBy fields(spring JPA Auditing)
a VO or DTO class extends an Entity class,  it is a value object not an entity

create 4 annotaions for creeatBy, updateBy, loginIp, loginLocation fields 

    该类使用了以下设计模式：
    静态工厂方法模式（Static Factory Method）：from(...) 是命名的静态构造器，用于封装实例创建逻辑。
    值对象/数据传输对象（VO/DTO）模式：此类用于承载展示/传输数据，不作为实体映射。
    另外它使用了组合（has‑a）而不是继承实体（避免被 JPA 识别为表映射）。

a entity extends other entity
    该类使用了以下设计模式：
    继承（Inheritance）：UserEntity 继承自 BaseEntity，复用其公共字段和行为。
    实体模式（Entity Pattern）：此类作为 JPA 实体映射到数据库表，表示持久化数据对象。
    这种设计通过继承实现代码复用，同时保持实体的独立性和清晰的数据库映射关系。