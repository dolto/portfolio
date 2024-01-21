package spring.boot.portfolio.Controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import spring.boot.portfolio.Model.CategoryModel.LangCollection;
//import spring.boot.portfolio.Model.CategoryModel.PostModel.ContentMode;
import spring.boot.portfolio.Model.CategoryModel.PostModel.PostCollection;
//import spring.boot.portfolio.Model.CategoryModel.PostModel.PostContent;
import spring.boot.portfolio.Model.CategoryModel.SkillCollection;
import spring.boot.portfolio.Service.PostService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.atomic.AtomicInteger;

@Controller @RequestMapping("/Post")
public class PostController {
    @Autowired
    PostService postService;

//    @Value("${app.password}")
//    String password;

    @RequestMapping("/PostList") /*@ResponseBody*/
    public String PrintList(Model model, String search_value,
                            @RequestParam(name = "post_lang", required = false)List<String> post_langs,
                            @RequestParam(name = "post_skill", required = false)List<String> post_skills){
        List<PostCollection> postCollections = postService.findAll();
        List<LangCollection> Langs = postService.findLangAll();
        List<SkillCollection> Skills = postService.findSkillAll();
        if(post_langs == null) post_langs = new ArrayList<>();
        if(post_skills == null) post_skills = new ArrayList<>();

        if(!post_langs.isEmpty() || !post_skills.isEmpty()){
            List<String> finalPost_langs = post_langs;
            List<String> finalPost_skills = post_skills;
            postCollections = postCollections.stream().filter(post ->
               !finalPost_langs.stream().filter(lang ->
                       post.getLang_id().contains(lang)
               ).toList().isEmpty()
                    ||
               !finalPost_skills.stream().filter(skill ->
                       post.getSkill_id().contains(skill)
               ).toList().isEmpty()
            ).toList();
        }
        if(!(search_value == null)){
            postCollections = postCollections.stream().filter(post ->
                    post.getName().contains(search_value)).toList();
        }

        model.addAttribute("posts", postCollections);
        model.addAttribute("Langs", Langs);
        model.addAttribute("Skills", Skills);
        model.addAttribute("Category_Langs", post_langs);
        model.addAttribute("Category_Skills", post_skills);
        model.addAttribute("SearchValue", search_value);
        return "Post/PostList";
    }

    @RequestMapping("/CategoryManager")
    public String CategoryManager(Model model){
        List<LangCollection> Langs = postService.findLangAll();
        List<SkillCollection> Skills = postService.findSkillAll();
        System.out.println(Langs);
        System.out.println(Skills);
        model.addAttribute("Langs", Langs);
        model.addAttribute("Skills", Skills);

        return "Post/CategoryManager";
    }
    @RequestMapping("/LangDeleteAction")
    public String LangDelete(String id){
        postService.deleteLang(id);
        System.out.println(id);
        return "redirect:CategoryManager";
    }
    @RequestMapping("/LangUpdateAction")
    public String LangUpdate(String id, String name, String img){
        postService.updateLang(id, name, img);
        return "redirect:CategoryManager";
    }
    @RequestMapping("/SkillDeleteAction")
    public String SkillDelete(String id){
        postService.deleteSkill(id);
        return "redirect:CategoryManager";
    }
    @RequestMapping("/SkillUpdateAction")
    public String SkillUpdate(String id, String name, String description, int level, String img){
        postService.updateSkill(id, name, description, level,img);
        return "redirect:CategoryManager";
    }

    @RequestMapping("/PostInsertPage")
    public String PostInsertPage(Model model, String id){
        List<LangCollection> Langs = postService.findLangAll();
        List<SkillCollection> Skills = postService.findSkillAll();
        model.addAttribute("Langs", Langs);
        model.addAttribute("Skills", Skills);
        if(id != null){
            PostCollection post = postService.findById(id);
            model.addAttribute("PostData", post);
//            model.addAttribute("PostStr", ContentMode.str);
//            model.addAttribute("PostImg", ContentMode.img);
//            model.addAttribute("PostLink", ContentMode.link);
        }
        //카테고리 이름 모음을 만들어서, 게시글 작성 시 존재하는 카테고리에 데이터를 추가할지 새로 만들지 선택 가능하게
        return "Post/PostInsert";
    }
    @RequestMapping("/PostInsertAction")
    public String PostInsertAction(String post_name,
                                   String id,
                                   String post_thumbnail,
//                                   @RequestParam(name = "post_type")List<String> post_type,
//                                   @RequestParam(name = "post_content")List<String> post_content,
                                   String post_content,
                                   @RequestParam(name = "post_lang")List<String> post_lang,
                                   @RequestParam(name = "post_skill")List<String> post_skill){

        if(post_lang.isEmpty() || post_skill.isEmpty() || post_thumbnail.isEmpty()){
            return "redirect:PostInsertPage";
        }
//        AtomicInteger count = new AtomicInteger();
//        List<PostContent> postContents = post_type.stream().map((t) -> {
//            PostContent p = new PostContent();
//            ContentMode m = ContentMode.str;
//            switch(t){
//                case "Img":
//                    m = ContentMode.img;
//                    is_somenail_img.set(true);
//                    break;
//                case "Link":
//                    m = ContentMode.link;
//                    break;
//            }
//            p.setMode(m);
//            p.setContent(post_content.get(count.get()));
//            count.addAndGet(1);
//            return p;
//        }).toList();
//        System.out.println(post_category);
//        System.out.println(post_skill);
        PostCollection tempPost = new PostCollection(post_name, post_thumbnail,post_content, post_lang, post_skill);
        if(id == null)
            postService.postSave(tempPost);
        else {
            tempPost.setId(id);
            tempPost.setWrite_day(postService.findById(id).getWrite_day());
            postService.postSave(tempPost);
        }
//        postService.CategoryInputPostId(postService.postSave(temp).getId(),post_category);

        return "redirect:PostInsertPage";
    }

    @RequestMapping("/AddLang")
    public void AddLang(String name, String img, HttpServletResponse res){
        postService.saveLang(name, img);
        try {
            res.setContentType("text/html; charset=UTF-8");
            PrintWriter out = res.getWriter();
            out.write("<script>" +
                    "history.back()" +
                    "</script>");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //return "redirect:PostInsertPage";
    }
    @RequestMapping("/AddSkill")
    public void AddSkill(String name, String description, int level, String img,
                         HttpServletResponse res){
        postService.saveSkill(name, description, level, img);
        try {
            res.setContentType("text/html; charset=UTF-8");
            PrintWriter out = res.getWriter();
            out.write("<script>" +
                    "history.back()" +
                    "</script>");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @RequestMapping("/PostDeleteAction")
    public String DeletePost(String id){
        postService.deletePostById(id);
        return "redirect:PostList";
    }
//    @RequestMapping("/Password")
//    public String Password(){
//        System.out.println(password);
//        return "redirect:/PostInsertPage";
//    }
}
