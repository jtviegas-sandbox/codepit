package org.aprestos.labs.jaxrs.jerseyjee.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

public class Item implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("id")
  private String id = null;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("price")
  private BigDecimal price = null;

  @JsonProperty("category")
  private String category = null;

  @JsonProperty("subcategory")
  private String subcategory = null;

  @JsonProperty("notes")
  private String notes = null;

  @JsonProperty("images")
  private List<String> images = null;

  public Item id(String id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * 
   * @return id
   **/
  @JsonProperty("id")
  @ApiModelProperty(required = true, value = "")
  @NotNull
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Item name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * 
   * @return name
   **/
  @JsonProperty("name")
  @ApiModelProperty(example = "doggie", required = true, value = "")
  @NotNull
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Item price(BigDecimal price) {
    this.price = price;
    return this;
  }

  /**
   * Get price
   * 
   * @return price
   **/
  @JsonProperty("price")
  @ApiModelProperty(required = true, value = "")
  @NotNull
  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public Item category(String category) {
    this.category = category;
    return this;
  }

  /**
   * Get category
   * 
   * @return category
   **/
  @JsonProperty("category")
  @ApiModelProperty(example = "shafts", value = "")
  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public Item subcategory(String subcategory) {
    this.subcategory = subcategory;
    return this;
  }

  /**
   * Get subcategory
   * 
   * @return subcategory
   **/
  @JsonProperty("subcategory")
  @ApiModelProperty(example = "alternators", value = "")
  public String getSubcategory() {
    return subcategory;
  }

  public void setSubcategory(String subcategory) {
    this.subcategory = subcategory;
  }

  public Item notes(String notes) {
    this.notes = notes;
    return this;
  }

  /**
   * Get notes
   * 
   * @return notes
   **/
  @JsonProperty("notes")
  @ApiModelProperty(value = "")
  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public Item images(List<String> images) {
    this.images = images;
    return this;
  }

  public Item addImagesItem(String imagesItem) {
    if (this.images == null) {
      this.images = new ArrayList<String>();
    }
    this.images.add(imagesItem);
    return this;
  }

  /**
   * Get images
   * 
   * @return images
   **/
  @JsonProperty("images")
  @ApiModelProperty(value = "")
  public List<String> getImages() {
    return images;
  }

  public void setImages(List<String> images) {
    this.images = images;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Item item = (Item) o;
    return Objects.equals(this.id, item.id) && Objects.equals(this.name, item.name)
        && Objects.equals(this.price, item.price) && Objects.equals(this.category, item.category)
        && Objects.equals(this.subcategory, item.subcategory) && Objects.equals(this.notes, item.notes)
        && Objects.equals(this.images, item.images);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, price, category, subcategory, notes, images);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Item {\n");

    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    price: ").append(toIndentedString(price)).append("\n");
    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("    subcategory: ").append(toIndentedString(subcategory)).append("\n");
    sb.append("    notes: ").append(toIndentedString(notes)).append("\n");
    sb.append("    images: ").append(toIndentedString(images)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
