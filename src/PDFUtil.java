package com.fh.util;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * pdf转换工具类
 * 
 * @author hujp
 * @date 2017/02/25
 *
 */
public class PDFUtil {

	protected static Logger logger = LoggerFactory.getLogger(PDFUtil.class);

	public static void main(String[] args) {
		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put("by_address", "Maven");

			Map<String, String> rs = doFillTemplatePDF("D:/report.pdf", "d:/report-out.pdf", map);
			System.out.println(rs);
			// pdf2Image("C:\\Users\\fangjinsuo.com\\Desktop\\14884527078601488452700547.pdf",
			// "E:\\out.png", 100);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 拼接多张图片
	 * 
	 * @param pics
	 *            图片集合
	 * @param dst_pic
	 *            图片拼接后存储路径
	 * @return 是否拼接成功
	 */
	public static boolean merge(List<File> pics, String dst_pic) {
		if (pics == null || pics.isEmpty()) {
			logger.error("pics is empty!");
			return false;
		}

		List<BufferedImage> list = new ArrayList<BufferedImage>();

		for (Iterator<File> iter = pics.iterator(); iter.hasNext();) {
			File f = iter.next();

			try {
				list.add(ImageIO.read(f));
			} catch (IOException e) {
				logger.error("文件读取失败！文件名称：" + f.getName(), e);
			}
		}

		return mergeImage(list, dst_pic);
	}

	/**
	 * 拼接多张图片
	 *
	 *            图片集合
	 *            图片类型
	 * @param dst_pic
	 *            图片拼接后存储路径
	 * @return 是否拼接成功
	 */
	public static boolean mergeImage(List<BufferedImage> images, String dst_pic) {
		if (images == null || images.isEmpty()) {
			logger.error("pics is empty!");
			return false;
		}

		int dst_height = 0;
		int dst_width = images.get(0).getWidth();
		int[][] ImageArrays = new int[images.size()][];

		for (int i = 0; i < images.size(); i++) {
			int width = images.get(i).getWidth();
			int height = images.get(i).getHeight();
			ImageArrays[i] = new int[width * height];// 从图片中读取RGB
			ImageArrays[i] = images.get(i).getRGB(0, 0, width, height, ImageArrays[i], 0, width);

			dst_width = dst_width > images.get(i).getWidth() ? dst_width : images.get(i).getWidth();

			dst_height += images.get(i).getHeight();
		}
		logger.info("拼接图片成功！图片高：{}，图片宽：{}", dst_height, dst_width);
		if (dst_height < 1) {
			logger.error("dst_height < 1");
			return false;
		}

		// 生成新图片
		try {
			// dst_width = images[0].getWidth();
			BufferedImage ImageNew = new BufferedImage(dst_width, dst_height, BufferedImage.TYPE_INT_RGB);
			int height_i = 0;
			for (int i = 0; i < images.size(); i++) {
				ImageNew.setRGB(0, height_i, dst_width, images.get(i).getHeight(), ImageArrays[i], 0, dst_width);
				height_i += images.get(i).getHeight();
			}

			File outFile = new File(dst_pic);
			ImageIO.write(ImageNew, "PNG", outFile);// 写图片
			logger.info("图片合成成功！图片：{}", dst_pic);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("拼接图片失败！", e);
			return false;
		}
		return true;
	}

	/***
	 * PDF文件转PNG图片，全部页数
	 * 
	 * @param PdfFilePath
	 *            pdf文件
	 * @param dstImgFile
	 *            图片文件地址
	 * @param dpi
	 *            转换像素
	 * @return
	 */
	public static boolean pdf2Image(String PdfFilePath, String dstImgFile, int dpi) {
		File file = new File(PdfFilePath);
		PDDocument pdDocument;
		try {
			pdDocument = PDDocument.load(file);
			PDFRenderer renderer = new PDFRenderer(pdDocument);
			/* 0表示第一页，300表示转换dpi，dpi越大转换后越清晰，相对转换速度越慢 */
			PdfReader reader = new PdfReader(PdfFilePath);
			int pages = reader.getNumberOfPages();

			// 声明一个图片集合
			List<BufferedImage> images = new ArrayList<BufferedImage>();

			for (int i = 0; i < pages; i++) {
				BufferedImage image = renderer.renderImageWithDPI(i, dpi);

				images.add(image);
			}

			// 多张图片合成一张
			mergeImage(images, dstImgFile);

			return true;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		return false;
	}

	/***
	 * PDF文件转PNG图片，全部页数
	 * 
	 * @param url
	 *            pdf文件远程URL
	 * @param dstImgFile
	 *            图片文件地址
	 * @param dpi
	 *            转换像素
	 * @return
	 * @throws IOException
	 */
	public static boolean pdf2ImageToURL(URL url, String dstImgFile, int dpi) throws IOException {
		PDDocument pdDocument = null;
		try {
			PdfReader reader = new PdfReader(url);
			// URL urlfile = new URL(url);
			HttpURLConnection httpUrl = (HttpURLConnection) url.openConnection();
			httpUrl.connect();
			pdDocument = PDDocument.load(httpUrl.getInputStream());

			PDFRenderer renderer = new PDFRenderer(pdDocument);
			/* 0表示第一页，300表示转换dpi，dpi越大转换后越清晰，相对转换速度越慢 */
			int pages = reader.getNumberOfPages();

			// 声明一个图片集合
			List<BufferedImage> images = new ArrayList<BufferedImage>();

			for (int i = 0; i < pages; i++) {
				BufferedImage image = renderer.renderImageWithDPI(i, dpi);

				images.add(image);
			}

			// 多张图片合成一张
			mergeImage(images, dstImgFile);

			return true;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (pdDocument != null) {
				// 关闭PDF Document
				pdDocument.close();
			}
		}

		return false;
	}

	/**
	 * 赋值并生成新的PDF文档
	 * 
	 * @param templatePDF
	 *            PDF模版
	 * @param outFile
	 *            输出的PDF Name
	 * @param hashMap
	 *            templatePDF对应的数据
	 */
	public static Map<String, String> doFillTemplatePDF(String templatePDF, String outPDFFile,
			Map<String, String> hashMap) {

		Map<String, String> map = new LinkedHashMap<String, String>();
		try {

			// 申明对象
			FileOutputStream fos = new FileOutputStream(outPDFFile);
			PdfReader reader = new PdfReader(templatePDF);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfStamper stamp = new PdfStamper(reader, baos);

			// 获得pdf表单域，并且填充内容
			AcroFields form = stamp.getAcroFields();
			form = setField(form, hashMap);
			stamp.setFormFlattening(true);
			stamp.close();

			// 将pdf填充到新pdf文件
			Document doc = new Document();
			PdfCopy pdfCopy = new PdfCopy(doc, fos);
			doc.open();

			int page = reader.getNumberOfPages();
			for (int c = 1; c <= page; c++) {
				PdfImportedPage importPage = pdfCopy.getImportedPage(new PdfReader(baos.toByteArray()), c);
				pdfCopy.addPage(importPage);
			}

			doc.close();
			reader.close();
			StreamUtil.close(fos);
			StreamUtil.close(baos);

			map.put("errCode", "0");
			map.put("msg", "PDF模板填充成功！");
			map.put("dstPDF", outPDFFile);
		} catch (FileNotFoundException e) {
			map.put("errCode", "1");
			map.put("msg", "PDF模板填充失败:" + e.getMessage());
			map.put("dstPDF", "");
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			map.put("errCode", "1");
			map.put("msg", "PDF模板填充失败:" + e.getMessage());
			map.put("dstPDF", "");
			logger.error(e.getMessage(), e);
		} catch (DocumentException e) {
			map.put("errCode", "1");
			map.put("msg", "PDF模板填充失败:" + e.getMessage());
			map.put("dstPDF", "");
			logger.error(e.getMessage(), e);
		}
		return map;

	}

	/***
	 * 通过PDF模板表单域填充
	 * 
	 * @param form
	 * @param fieldMap
	 * @return
	 */
	private static AcroFields setField(AcroFields form, Map<String, String> fieldMap) {
		Set<String> it = form.getFields().keySet();

		for (Iterator<String> iter = it.iterator(); iter.hasNext();) {
			String key = iter.next();

			try {
				form.setField(key, fieldMap.get(key));
			} catch (IOException e) {
				logger.error("Field IO error!", e);
			} catch (DocumentException e) {
				logger.error("Field Document error!  ", e);
			}
		}

		return form;
	}

}
