package com.similarimage.tools;

import com.intellij.util.ui.UIUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @description: 图片相似度判断算法
 * @author: tingyuan
 * @date: 2022/9/13
 */
public class SimilarImageAlgorithm {
    public static List<ImageInfo> imageInfoList = new ArrayList<>();
    private static int threshold = 95;
    public static void main(String[] args) {

    }

    public static HashMap<String,ArrayList<String>> checkImage(String path) {
        imageInfoList.clear();
        File f = new File(path);
        tree(f, 1);
        HashMap<String,ArrayList<String>> result = new HashMap<>();
        if (!imageInfoList.isEmpty()) {
            for (int i = 0; i < imageInfoList.size(); i ++) {
                ImageInfo tempLeft = imageInfoList.get(i);
                result.put(tempLeft.path, new ArrayList<>());
                for (int j = i + 1; j < imageInfoList.size(); j ++) {
                    ImageInfo tempRight = imageInfoList.get(j);
                    try {
                        double similar = getSimilarity(new File(tempLeft.path), new File(tempRight.path));
                        if (similar >= threshold) {
                            ArrayList<String> similarNameList = result.get(tempLeft.path);
                            similarNameList.add(tempRight.path);
                            result.put(tempLeft.path, similarNameList);
                        }
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
        return result;
    }

    public static void tree(File f, int level) {
        File[] childArrays = f.listFiles();
        if (childArrays == null || childArrays.length == 0) {
            return;
        }
        for (File childArray : childArrays) {
            if (childArray.isDirectory()) {
                tree(childArray, level + 1);
            } else {
                String filename = childArray.getName();
                String fileSuffix = filename.substring(filename.lastIndexOf(".") + 1);
                if ("jpg".equals(fileSuffix) || "jpeg".equals(fileSuffix) || "png".equals(fileSuffix) || "webp".equals(fileSuffix)) {
                    ImageInfo imageInfo = new ImageInfo();
                    imageInfo.name = filename;
                    imageInfo.path = childArray.getPath();
                    try {
                        imageInfo.fingerPrint = getImgFinger(childArray);
                        imageInfoList.add(imageInfo);
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }

        }

    }

    /**
     * 对比两张图的获取相似度
     */
    public static double getSimilarity(File imageFile1, File imageFile2) throws IOException {
        int[] pixels1 = getImgFinger(imageFile1);
        int[] pixels2 = getImgFinger(imageFile2);
        int hammingDistance = getHammingDistance(pixels1, pixels2);
        return calSimilarity(hammingDistance) * 100;

    }

    /**
     * 获取图片的指纹
     */
    private static int[] getImgFinger(File imageFile) throws IOException {
        Image image = ImageIO.read(imageFile);
        // 转换至灰度
        image = toGrayscale(image);
        // 缩小成32x32的缩略图，这里也可以缩略成8*8
        image = scale(image);
        // 获取灰度像素数组
        int[] pixels1 = getPixels(image);
        // 获取平均灰度颜色
        int averageColor = getAverageOfPixelArray(pixels1);
        // 获取灰度像素的比较数组(即图像指纹序列)
        pixels1 = getPixelDeviateWeightsArray(pixels1, averageColor);
        return pixels1;

    }

    /**
     * 将任意Image类型图像转换为BufferedImage类型
     */
    public static BufferedImage convertToBufferedFrom(Image srcImage) {
        BufferedImage bufferedImage = new BufferedImage(srcImage.getWidth(null),
                srcImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedImage.createGraphics();
        g.drawImage(srcImage, null, null);
        g.dispose();
        return bufferedImage;

    }

    /**
     * 转换至灰度图
     */
    public static BufferedImage toGrayscale(Image image) {
        BufferedImage sourceBuffered = convertToBufferedFrom(image);
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorConvertOp op = new ColorConvertOp(cs, null);
        BufferedImage grayBuffered = op.filter(sourceBuffered, null);
        return grayBuffered;

    }

    /**
     * 缩放至32x32像素缩略图
     */
    public static Image scale(Image image) {
        image = image.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
        return image;

    }

    /**
     * 获取像素数组
     */
    public static int[] getPixels(Image image) {
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        return convertToBufferedFrom(image).getRGB(0, 0, width, height,
                null, 0, width);

    }


    /**
     * 获取灰度图的平均像素颜色值
     */
    public static int getAverageOfPixelArray(int[] pixels) {
        Color color;
        long sumRed = 0;
        for (int i = 0; i < pixels.length; i++) {
            color = new Color(pixels[i], true);
            sumRed += color.getRed();
        }
        return (int) (sumRed / pixels.length);

    }

    /**
     * 获取灰度图的像素比较数组(平均值的离差)
     */
    public static int[] getPixelDeviateWeightsArray(int[] pixels, final int averageColor) {
        Color color;
        int[] dest = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            color = new Color(pixels[i], true);
            dest[i] = color.getRed() - averageColor > 0 ? 1 : 0;

        }

        return dest;

    }


    /**
     * 获取两个缩略图的平均像素比较数组的汉明距离(距离越大差异越大)
     *
     */
    public static int getHammingDistance(int[] a, int[] b) {
        int sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] == b[i] ? 0 : 1;

        }
        return sum;

    }

    /**
     * 通过汉明距离计算相似度
     */
    public static double calSimilarity(int hammingDistance) {
        int length = 32 * 32;
        double similarity = (length - hammingDistance) / (double) length;
        // 使用指数曲线调整相似度结果
        similarity = java.lang.Math.pow(similarity, 2);
        return similarity;

    }
}
