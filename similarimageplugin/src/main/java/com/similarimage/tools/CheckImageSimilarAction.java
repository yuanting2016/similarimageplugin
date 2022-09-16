package com.similarimage.tools;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class CheckImageSimilarAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);

        String text = Messages.showInputDialog(project,
                "请输入待检测文件的绝对路径",
                "",
                Messages.getQuestionIcon());
        HashMap<String, ArrayList<String>> result = SimilarImageAlgorithm.checkImage(text);
        writeFile(text, result);
    }

    private void writeFile (String path, HashMap<String, ArrayList<String>> result) {
        String filePath = path + File.separator + System.currentTimeMillis()  + ".txt";
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
                assert fw != null;
                fw.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
