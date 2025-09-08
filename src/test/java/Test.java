import me.manuloff.lib.table.renderer.Text;
import me.manuloff.lib.table.renderer.style.Alignment;
import me.manuloff.lib.table.renderer.style.Padding;
import me.manuloff.lib.table.renderer.Table;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;

/**
 * @author Manuloff
 * @since 14:14 21.01.2025
 */
public class Test {

	public static void main(String[] args) throws IOException {
		Table table = new Table();

		table.tableStyle()
				.borderColor(new Color(0xDDDDDD))
				.borderWidth(1)
				.backgroundColor(Color.WHITE)
				.padding(new Padding(4, 4, 14, 14))
				.minCellHeight(10)
				.minCellWidth(20);

		table.textStyle()
				.fontName("Arial")
				.lineHeight(1F)
				.fontStyle(Font.PLAIN)
				.fontSize(14)
				.textColor(Color.decode("#333333"))
				.textAlignment(Alignment.CENTER);

		// Первая строка - header

		table.getRow(0)
				.textStyle(style -> {
					style.fontSize(17).fontStyle(Font.BOLD);
				})
				.tableStyle(style -> {
					style.backgroundColor(new Color(0xE9E9E9))
							.padding(new Padding(10));
				});

		table.getCell(0, 0).setText("№\nпары");
		table.getCell(0, 1, 1, 2).setText("Пн\n08.09");
		table.getCell(0, 2).setText("Вт\n09.09");
		table.getCell(0, 3).setText("Ср\n10.09");
		table.getCell(0, 4).setText("Чт\n11.09");
		table.getCell(0, 5).setText("Пт\n12.09");
		table.getCell(0, 6).setText("Сб\n13.09");

		// Первая колонка - пары и их время
		table.getCell(1, 0)
				.appendText("1")
				.appendNewLine()
				.appendText(new Text("08:10 -\n09:40").textStyle(style -> style.fontStyle(Font.BOLD)));

		table.getCell(2, 0)
				.appendText("2")
				.appendNewLine()
				.appendText(new Text("09:50 -\n11:20").textStyle(style -> style.fontStyle(Font.BOLD)));

		table.getCell(3, 0)
				.appendText("3")
				.appendNewLine()
				.appendText(new Text("11:30 -\n13:00").textStyle(style -> style.fontStyle(Font.BOLD)));

		table.getCell(4, 0)
				.appendText("4")
				.appendNewLine()
				.appendText(new Text("13:30 -\n15:00").textStyle(style -> style.fontStyle(Font.BOLD)));

		table.getCell(5, 0)
				.appendText("5")
				.appendNewLine()
				.appendText(new Text("15:10 -\n16:40").textStyle(style -> style.fontStyle(Font.BOLD)));

		table.getCell(6, 0)
				.appendText("6")
				.appendNewLine()
				.appendText(new Text("16:50 -\n18:20").textStyle(style -> style.fontStyle(Font.BOLD)));

		table.getCell(7, 0)
				.appendText("7")
				.appendNewLine()
				.appendText(new Text("18:30 -\n20:00").textStyle(style -> style.fontStyle(Font.BOLD)));

		table.getCell(8, 0)
				.appendText("8")
				.appendNewLine()
				.appendText(new Text("20:10 -\n21:40").textStyle(style -> style.fontStyle(Font.BOLD)));

//		// Расписание
//
//		for (int i = 1; i < 7; i++) {
//			for (int j = 1; j < 9; j++) {
//				if (new Random().nextInt(5) < 4) {
//					continue;
//				}
//
//				table.getCell(j, i)
//						.appendText(new Text("МатАН")
//								.textStyle(style -> style.fontStyle(Font.BOLD))
//						)
//						.appendNewLine()
//						.appendText("Лекции")
//						.appendNewLine()
//						.appendText(new Text("Сташкевич М.В.")
//								.textStyle(style -> style.textColor(new Color(0x1976d2)))
//						)
//						.appendNewLine()
//						.appendText(new Text("301/3")
//								.textStyle(style -> style.fontStyle(Font.BOLD))
//						);
//			}
//		}

		table.getCell(1, 1)
				.appendText(new Text("МатАН")
						.textStyle(style -> style.fontStyle(Font.BOLD))
				)
				.appendNewLine()
				.appendText("Лекции")
				.appendNewLine()
				.appendText(new Text("Сташкевич М.В.")
						.textStyle(style -> style.textColor(new Color(0x1976d2)))
				)
				.appendNewLine()
				.appendText(new Text("301/3")
						.textStyle(style -> style.fontStyle(Font.BOLD))
				);

		table.getCell(1, 2)
				.appendText(new Text("МатАН")
						.textStyle(style -> style.fontStyle(Font.BOLD))
				)
				.appendNewLine()
				.appendText("Лекции")
				.appendNewLine()
				.appendText(new Text("Сташкевич М.В.")
						.textStyle(style -> style.textColor(new Color(0x1976d2)))
				)
				.appendNewLine()
				.appendText(new Text("301/3")
						.textStyle(style -> style.fontStyle(Font.BOLD))
				);

		table.getCell(2, 1, 1, 2)
				.appendText(new Text("МатАН")
						.textStyle(style -> style.fontStyle(Font.BOLD))
				)
				.appendNewLine()
				.appendText("Лекции")
				.appendNewLine()
				.appendText(new Text("Сташкевич М.В.")
						.textStyle(style -> style.textColor(new Color(0x1976d2)))
				)
				.appendNewLine()
				.appendText(new Text("301/3")
						.textStyle(style -> style.fontStyle(Font.BOLD))
				);

		long start = System.currentTimeMillis();

		BufferedImage image = table.render().getImage();
		ImageIO.write(image, "png", new File("table.png"));

		System.out.println("Completed in " + (System.currentTimeMillis() - start) + "ms");

//		byte[] imageBytes;
//
//		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
//			ImageIO.write(image, "png", baos);
//			imageBytes = baos.toByteArray();
//		}
//
//		System.out.println("Completed in " + (System.currentTimeMillis() - start) + "ms");
//		start = System.currentTimeMillis();
//
//		String hash = hashBytes(imageBytes);
//
//		System.out.println("hash = " + hash);
//		System.out.println("Completed in " + (System.currentTimeMillis() - start) + "ms");
	}

	private static String hashBytes(byte[] bytes) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(bytes);
			StringBuilder hex = new StringBuilder();
			for (byte b : hash) {
				hex.append(String.format("%02x", b));
			}
			return hex.toString();
		} catch (Exception e) {
			throw new RuntimeException("Unable to hash bytes", e);
		}
	}
}
