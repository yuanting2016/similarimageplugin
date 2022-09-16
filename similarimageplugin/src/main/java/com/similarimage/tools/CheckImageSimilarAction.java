package com.similarimage.tools;

import com.fasterxml.jackson.jr.ob.JSON;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import kotlinx.serialization.json.Json;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class CheckImageSimilarAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);

        String text = Messages.showInputDialog(project,
                "请输入待检测图片的文件相对路径",
                "",
                Messages.getQuestionIcon());
        HashMap<String, ArrayList<String>> result = SimilarImageAlgorithm.checkImage(text);
        writeFile(text, result, e);
//        Messages.showMessageDialog(project,
//                "What you have entered is:" + text,
//                "Information",
//                Messages.getInformationIcon());
    }

    private void writeFile (String path, HashMap<String, ArrayList<String>> result, AnActionEvent event) {
        String filePath = "/Users/codoon/Documents/project/SomeTest" + File.separator + System.currentTimeMillis()  + ".txt";
        System.out.println(filePath);
        StringBuilder content = new StringBuilder();
        if (result != null) {
            for (String key : result.keySet()) {
                ArrayList<String> imageList = result.get(key);
                if (imageList != null && !imageList.isEmpty()) {
                    StringBuilder similarList = new StringBuilder();
                    for (String name: imageList) {
                        similarList.append(name);
                        similarList.append("\n");
                    }
                    content.append(key).append("相似的图片有：\n").append(similarList);
                }
            }
        }
//        Project project = event.getData(PlatformDataKeys.PROJECT);
//                Messages.showMessageDialog(project,
//                content.toString(),
//                "result",
//                Messages.getInformationIcon());
        System.out.println(content.toString());
        FileWriter fw = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(filePath);
            fw.write(String.valueOf(content));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fw.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
