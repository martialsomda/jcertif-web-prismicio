package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import views.html.*;

import io.prismic.*;
import static controllers.Prismic.*;

public class Application extends Controller {

  // -- Resolve links to documents
  final public static LinkResolver linkResolver(Api api, String ref, Http.Request request) {
    return new LinkResolver(api, ref, request);
  } 

  public static class LinkResolver extends DocumentLinkResolver {
    final Api api;
    final String ref;
    final Http.Request request;

    public LinkResolver(Api api, String ref, Http.Request request) {
      this.api = api;
      this.ref = ref;
      this.request = request;
    }

    public String resolve(Fragment.DocumentLink link) {
    	// For "Bookmarked" documents that use a special page
        if(isBookmark(api, link, "about")) {
          return routes.Application.about(ref).absoluteURL(request);
        }
        else if(isBookmark(api, link, "getinvolved")) {
          return routes.Application.getinvolved(ref).absoluteURL(request);
        }
        else if(isBookmark(api, link, "communities")) {
          return routes.Application.communities(ref).absoluteURL(request);
        }

        // Events documents
        else if("conference".equals(link.getType()) && !link.isBroken()) {
          return routes.Application.eventDetail(link.getId(), link.getSlug(), ref).absoluteURL(request);
        }

        // Any project
        else if("project".equals(link.getType()) && !link.isBroken()) {
          return routes.Application.projectDetail(link.getId(), link.getSlug(), ref).absoluteURL(request);
        } 
        
        // Any community
        else if("community".equals(link.getType()) && !link.isBroken()) {
          return routes.Application.communityDetail(link.getId(), link.getSlug(), ref).absoluteURL(request);
        }
        
      return routes.Application.detail(link.getId(), link.getSlug(), ref).absoluteURL(request);
    }
  }

  // -- Page not found
  static Result pageNotFound() {
    return notFound("Page not found");
  }
  
  // -- Home page
  @Prismic.Action
  public static Result index(String ref) {
    List<Document> events = prismic().getApi().getForm("events").ref(prismic().getRef()).submit();
    List<Document> globalSponsors = prismic().getApi().getForm("global-sponsors").ref(prismic().getRef()).submit();
    List<Document> globalProjects = prismic().getApi().getForm("global-projects").ref(prismic().getRef()).submit();
    return ok(views.html.index.render(events, globalProjects, globalSponsors));
  }

  //-- About us

  @Prismic.Action
  public static Result about(String ref) {
    Document aboutPage = prismic().getBookmark("about");
    if(aboutPage == null) {
      return pageNotFound();
    } else {
      return ok(views.html.about.render(aboutPage));
    }
  }

  // -- Get Involved

  @Prismic.Action
  public static Result getinvolved(String ref) {
    Document contributionPage = prismic().getBookmark("contribute");
    if(contributionPage == null) {
      return pageNotFound();
    } else {
      return ok();
    }
  }
  
  //-- Communities

  @Prismic.Action
  public static Result communities(String ref) {
    Document communitiesPage = prismic().getBookmark("communities");
    if(communitiesPage == null) {
      return pageNotFound();
    } else {
      List<Document> communities = prismic().getApi().getForm("communities").ref(prismic().getRef()).submit();
      return ok(views.html.communities.render(communitiesPage, communities));
    }
  }
  
  //-- Community Selections

  @Prismic.Action
  public static Result communityDetail(String id, String slug, String ref) {
    Document selection = prismic().getDocument(id);
    String checked = prismic().checkSlug(selection, slug);
    if(checked == DOCUMENT_NOT_FOUND) {
      return pageNotFound();
    }
    else if(checked == null) {
      List<String> productIds = new ArrayList<String>();
      for(Fragment fragment: selection.getAll("selection.communiy")) {
        Fragment.DocumentLink link = (Fragment.DocumentLink)fragment;
        if(link.getType().equals("communiy") && !link.isBroken()) {
          productIds.add(link.getId());
        }
      }
      List<Document> products = prismic().getDocuments(productIds);
      return ok();
    }
    else {
      return redirect(routes.Application.communityDetail(id, checked, ref));
    }
  }
  
  //-- Blog

  final public static String[] BlogCategories = new String[] {
    "Java EE", 
    "Embarque", 
    "Mobile",
    "Web",
    "Productivité"
  };

  @Prismic.Action
  public static Result blog(String category, String ref) {
    List<Document> posts;
    if(category == null) {
      posts = prismic().getApi().getForm("blog").ref(prismic().getRef()).submit();
    } else {
      posts = prismic().getApi().getForm("blog").query("[[:d = at(my.blog-post.category, \"" + category + "\")]]").ref(prismic().getRef()).submit();
    }
    Collections.sort(posts, new Comparator<Document>() {
      public int compare(Document d1, Document d2) {
        Long t1 = d1.getDate("blog-post.date") == null ? 0 : d1.getDate("blog-post.date").getValue().getMillis();
        Long t2 = d2.getDate("blog-post.date") == null ? 0 : d2.getDate("blog-post.date").getValue().getMillis();
        return t2.compareTo(t1);
      }
    });
    return ok();
  }
  
  @Prismic.Action
  public static Result events(String ref) {
    List<Document> events = prismic().getApi().getForm("events").ref(prismic().getRef()).submit();
    return ok(views.html.events.render(events, null));
  }
  
  //-- Event Selections

  @Prismic.Action
  public static Result eventDetail(String id, String slug, String ref) {
    Document selection = prismic().getDocument(id);
    String checked = prismic().checkSlug(selection, slug);
    if(checked == DOCUMENT_NOT_FOUND) {
      return pageNotFound();
    }
    else if(checked == null) {
      List<String> productIds = new ArrayList<String>();
      for(Fragment fragment: selection.getAll("selection.conference")) {
        Fragment.DocumentLink link = (Fragment.DocumentLink)fragment;
        if(link.getType().equals("conference") && !link.isBroken()) {
          productIds.add(link.getId());
        }
      }
      List<Document> products = prismic().getDocuments(productIds);
      return ok();
    }
    else {
      return redirect(routes.Application.eventDetail(id, checked, ref));
    }
  }
  
  //-- Project Selections

  @Prismic.Action
  public static Result projectDetail(String id, String slug, String ref) {
    Document selection = prismic().getDocument(id);
    String checked = prismic().checkSlug(selection, slug);
    if(checked == DOCUMENT_NOT_FOUND) {
      return pageNotFound();
    }
    else if(checked == null) {
      List<String> productIds = new ArrayList<String>();
      for(Fragment fragment: selection.getAll("selection.project")) {
        Fragment.DocumentLink link = (Fragment.DocumentLink)fragment;
        if(link.getType().equals("project") && !link.isBroken()) {
          productIds.add(link.getId());
        }
      }
      List<Document> products = prismic().getDocuments(productIds);
      return ok();
    }
    else {
      return redirect(routes.Application.projectDetail(id, checked, ref));
    }
  }
  
  // -- Document detail
  @Prismic.Action
  public static Result detail(String id, String slug, String ref) {
    Document maybeDocument = prismic().getDocument(id);
    String checked = prismic().checkSlug(maybeDocument, slug);
    if(checked == null) {
      return ok(views.html.detail.render(maybeDocument));
    }
    else if(checked == DOCUMENT_NOT_FOUND) {
      return pageNotFound();
    }
    else {
      return redirect(routes.Application.detail(id, checked, ref));
    }
  }

  // -- Basic Search
  @Prismic.Action
  public static Result search(String q, String ref) {
    List<Document> results = new ArrayList<Document>();
    if(q != null && !q.trim().isEmpty()) {
      results = prismic().getApi().getForm("everything").query("[[:d = fulltext(document, \"" + q + "\")]]").ref(prismic().getRef()).submit();
    }
    return ok(views.html.search.render(q, results));
  }
  
  //-- Conferences

  final public static Map<String,String> JCertifEventTypes = new LinkedHashMap<String,String>();
  static {
	  JCertifEventTypes.put("Conferences", "JCertif Conferences");
	  JCertifEventTypes.put("Other Events", "JCertif Other Events");
  }
  
}
