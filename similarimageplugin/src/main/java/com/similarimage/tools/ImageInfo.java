package com.similarimage.tools;

/**
 * @date :2022/9/15
 * @author:yuanting
 * @des: 图片信息
 */
class ImageInfo {
    public String path;
    public String name;
    public int[] fingerPrint;

    @Override
    public String toString() {
        return "ImageInfo{" +
                "path='" + path + '\'' +
                ", name='" + name + '\'' +
                ", fingerPrint=" + fingerPrint +
                '}';
    }
}
