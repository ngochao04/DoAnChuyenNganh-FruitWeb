package Fruit.Web.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "categories", uniqueConstraints = @UniqueConstraint(columnNames = "slug"))
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 120)
    @Column(columnDefinition = "text")   // pgadmin đang để text
    private String name;

    @NotBlank @Size(max = 160)
    @Column(columnDefinition = "text")
    private String slug;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    // getters & setters
    public Long getId(){ return id; }
    public void setId(Long id){ this.id = id; }

    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }

    public String getSlug(){ return slug; }
    public void setSlug(String slug){ this.slug = slug; }

    public String getDescription(){ return description; }
    public void setDescription(String description){ this.description = description; }

    public Long getParentId(){ return parentId; }
    public void setParentId(Long parentId){ this.parentId = parentId; }

    public Integer getSortOrder(){ return sortOrder; }
    public void setSortOrder(Integer sortOrder){ this.sortOrder = sortOrder; }

    public Boolean getActive(){ return active; }
    public void setActive(Boolean active){ this.active = active; }
}
