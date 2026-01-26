package com.celebrationpoint.backend.service.invoice;

import com.celebrationpoint.backend.entity.*;
import com.celebrationpoint.backend.exception.ResourceNotFoundException;
import com.celebrationpoint.backend.repository.OrderItemRepository;
import com.celebrationpoint.backend.repository.OrderRepository;
import com.celebrationpoint.backend.repository.PaymentRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class InvoiceService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    // ===============================
    // 🧾 ADMIN / INTERNAL USE
    // ===============================
    public byte[] generateInvoice(Long orderId) {

        Order order = getPaidOrder(orderId);

        return generatePdf(order);
    }

    // ===============================
    // 🧾 USER SAFE ACCESS
    // ===============================
    public byte[] generateInvoiceForUser(Long orderId, String email) {

        Order order = getPaidOrder(orderId);

        if (!order.getUser().getEmail().equals(email)) {
            throw new RuntimeException("You are not allowed to access this invoice");
        }

        return generatePdf(order);
    }

    // ===============================
    // ✅ PAYMENT VALIDATION (CORE FIX)
    // ===============================
    private Order getPaidOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new RuntimeException("Payment not found for order"));

        boolean paid =
                payment.getStatus() == PaymentStatus.SUCCESS ||
                payment.getStatus() == PaymentStatus.COD_PAID;

        if (!paid) {
            throw new RuntimeException("Invoice available only for paid orders");
        }

        return order;
    }

    // ===============================
    // 🧠 CORE PDF LOGIC (SAFE)
    // ===============================
    private byte[] generatePdf(Order order) {

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);

            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 12);

            document.add(new Paragraph("CELEBRATION POINT", titleFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Invoice", titleFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Order ID: " + order.getId(), normalFont));
            document.add(new Paragraph("Name: " + order.getFullName(), normalFont));
            document.add(new Paragraph("Phone: " + order.getPhone(), normalFont));
            document.add(new Paragraph(
                    "Address: " + order.getAddressLine() + ", "
                            + order.getCity() + ", "
                            + order.getState() + " - "
                            + order.getPincode(),
                    normalFont
            ));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Items:", titleFont));
            document.add(new Paragraph(" "));

            List<OrderItem> items = orderItemRepository.findByOrder(order);

            for (OrderItem item : items) {
                document.add(new Paragraph(
                        item.getProductName()
                                + " | Qty: " + item.getQuantity()
                                + " | Price: ₹" + item.getPrice(),
                        normalFont
                ));
            }

            document.add(new Paragraph(" "));
            document.add(new Paragraph(
                    "Total Amount: ₹" + order.getTotalAmount(),
                    titleFont
            ));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Thank you for shopping with us!", normalFont));

            document.close(); // ✅ REQUIRED

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }
}
