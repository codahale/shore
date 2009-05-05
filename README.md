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

Then, write a DAO interface:
    
    public interface WidgetDAO {
      public abstract Widget findByName(String name);
      public abstract Widget save(Widget widget);
    }

Then implement the DAO:
    
    public class WidgetDAOImpl extends AbstractDAO implements WidgetDAO {

      @Inject
      public WidgetDAOImpl(Provider<Session> provider) {
        super(provider);
      }

      @Transactional
      @Override
      public Widget findByName(String name) {
        final Criteria criteria = currentSession().createCriteria(Widget.class);
        criteria.add(Restrictions.eq("name", name));
        criteria.setMaxResults(1);
        return (Widget) criteria.uniqueResult();
      }

      @Transactional
      @Override
      public Widget save(Widget widget) {
        currentSession().save(widget);
        return widget;
      }
      
    }
    
And then associate the DAO interface with the implementation:
    
    @ImplementedBy(WidgetDAOImpl.class)
    public interface WidgetDAO {
      // ...
    }

Then write a resource:
    
    @Path("/widget/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public class WidgetResource {
      private final WidgetDAO widgetDAO;

      @Inject
      public WidgetResource(WidgetDAO widgetDAO) {
        this.widgetDAO = widgetDAO;
      }

      @GET
      @Transactional
      public String getWidget(@PathParam("name") String name) {
        final Widget widget = widgetDAO.findByName(name);

        if (widget != null) {
          return "[Widget name:" + widget.getName() + ", description:" + widget.getDescription() + "]";
        }

        throw new WebApplicationException(Response.status(Status.NOT_FOUND).build());
      }

      @POST
      @Transactional
      public Response makeWidget(@Context UriInfo uriInfo,
          @PathParam("name") String name,
          String description) {

        final Widget widget = new Widget();
        widget.setName(name);
        widget.setDescription(description);
        widgetDAO.save(widget);

        return Response.created(uriInfo.getBaseUriBuilder().path(WidgetResource.class).build(name)).build();
      }
    }

And add your configuration:
    
    class WidgetApiConfig extends AbstractConfiguration {
      @Override
      protected void configure() {
        addResourcePackage("<name of the package where WidgetResource is>");
        addEntityPackage("<name of the package where Widget is>");
      }

      @Override
      public String getExecutableName() {
        return "widget-api";
      }
    }

Then write your main class:
    
    public class WidgetAPI {
      public static void main(String[] args) throws Exception {
        Shore.run(new WidgetApiConfig(), args);
      }
    }

And add some configuration details to `development.properties`:
    
    hibernate.connection.username=sa
    hibernate.connection.password=
    hibernate.connection.pool_size=1
    hibernate.dialect=org.hibernate.dialect.HSQLDialect
    hibernate.connection.url=jdbc:hsqldb:mem:WidgetDB
    hibernate.connection.driver_class=org.hsqldb.jdbcDriver
    hibernate.hbm2ddl.auto=create-drop

Now you're ready to generate a schema:
    
    java -jar widget-api.jar schema --config=development.properties

Or generate a migration script, if the database already has stuff in it:
    
    java -jar widget-api.jar schema --config=development.properties --migration

Then run it:
    
    java -jar widget-api.jar server --config=development.properties --port=8080

And use it:
    
    curl http://0.0.0.0:8080/widgets/hoople
    curl --data-binary="A hoople." http://0.0.0.0:8080/widgets/hoople
    curl http://0.0.0.0:8080/widgets/hoople
