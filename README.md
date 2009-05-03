Shore
=====

*What makes Jersey fun.*

Shore is a web application framework which ties together
[Jersey](https://jersey.dev.java.net/), [Hibernate](https://www.hibernate.org/),
and [Guice](http://code.google.com/p/google-guice/) into an awesome foundation
for data-backed, RESTful web services in Java.

How To Write A Shore Application
--------------------------------

First, write an entity class:

{% highlight java %}
@Entity
@Table(name="widgets")
public class Widget {
  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  private Integer id;
  
  @Column(name="name")
  private String name;
  
  @Column(name="description")
  private String description;
  
  public Integer getId() { return id; }
  
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
}      
{% end %}

