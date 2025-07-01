package com.deardream.deardream_be.domain.archive.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class PdfTest {
    public static void main(String[] args) {
        try {
            // 1. Thymeleaf 설정
            ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
            resolver.setPrefix("templates/");
            resolver.setSuffix(".html");
            resolver.setTemplateMode("HTML5");
            resolver.setCacheable(false);
            resolver.setCharacterEncoding("UTF-8");

            TemplateEngine engine = new TemplateEngine();
            engine.setTemplateResolver(resolver);

            // 2. 데이터 바인딩
            Context context = new Context();
            context.setVariable("name", "김인호");
            context.setVariable("relation", "아들");
            context.setVariable("text", "안녕하세요. 한번 pdf로 만들어봤는데 잘 나오나요? 확인하고 싶었어요. 잘 나오는지 궁금해요");
            //File imageFile1 = new File("src/main/resources/images/test1.jpeg");
            //File imageFile2 = new File("src/main/resources/images/test1.jpeg");

            //System.out.println(imageFile1.exists()); // true여야 함
            //System.out.println(imageFile1.toURI());

            //context.setVariable("image1", "http://localhost:8080/images/test1.jpeg");
            //context.setVariable("image2", "http://localhost:8080/images/test1.jpeg");

            // 3. HTML 렌더링
            String htmlContent = engine.process("template", context);
            System.out.println(htmlContent);

            // 4. PDF 변환
            String fontPath = "src/main/resources/fonts/NotoSansKR-VariableFont_wght.ttf";
            File output = new File("output.pdf");

            try (OutputStream os = new FileOutputStream(output)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(htmlContent, null);
                builder.useFont(new File(fontPath), "Noto Sans KR");
                builder.toStream(os);
                builder.run();
            }

            System.out.println("✅ PDF 생성 완료: " + output.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
