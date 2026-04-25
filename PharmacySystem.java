package care4u;

import java.io.*;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class PharmacySystem extends JFrame {

    private final ArrayList<Product> productList = new ArrayList<>();
    private final ArrayList<String> salesHistory = new ArrayList<>();

    // Inventory
    private JTextField txtId, txtName, txtPrice, txtQuantity;
    private JTable table;
    private DefaultTableModel tableModel;

    // POS
    private JTextField txtSaleId, txtSaleQty;
    private JLabel lblTotal;
    private JComboBox<String> cmbPayment;

    // History
    private JTextArea txtHistoryArea;

    private void saveToFile() {
        try {
            PrintWriter writer = new PrintWriter("products.txt");

            for (Product p : productList) {
                writer.println(
                        p.getId() + "," +
                                p.getName() + "," +
                                p.getPrice() + "," +
                                p.getQuantity()
                );
            }

            writer.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving data.");
        }
    }

    private void loadFromFile() {
        try {
            File file = new File("products.txt");

            if (!file.exists()) return;

            productList.clear();   // ✅ VERY IMPORTANT

            Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] data = line.split(",");

                Product p = new Product(
                        data[0],
                        data[1],
                        Double.parseDouble(data[2]),
                        Integer.parseInt(data[3])
                );

                productList.add(p);
            }

            sc.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading data.");
        }
    }


    public PharmacySystem() {
        loadFromFile();// let data store in a file
        setTitle("Care4U Pharmacy System");
        setSize(950, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);



        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Inventory", createInventoryPanel());
        tabs.add("Point of Sales", createPOSPanel());
        tabs.add("Sales History", createHistoryPanel());

        add(tabs);
        setVisible(true);
    }

    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Product Details"));

        formPanel.add(new JLabel("Product ID:"));
        txtId = new JTextField();
        formPanel.add(txtId);

        formPanel.add(new JLabel("Product Name:"));
        txtName = new JTextField();
        formPanel.add(txtName);

        formPanel.add(new JLabel("Price (RM):"));
        txtPrice = new JTextField();
        formPanel.add(txtPrice);

        formPanel.add(new JLabel("Quantity:"));
        txtQuantity = new JTextField();
        formPanel.add(txtQuantity);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAdd = new JButton("Add");
        JButton btnSearch = new JButton("Search");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");
        JButton btnLogout = new JButton("Logout");

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnSearch);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnLogout);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        tableModel = new DefaultTableModel(new String[]{"Product ID", "Product Name", "Price", "Quantity"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        JLabel lblView = new JLabel("View All Products");
        lblView.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.add(lblView, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> addProduct());
        btnSearch.addActionListener(e -> searchProduct());
        btnUpdate.addActionListener(e -> updateProduct());
        btnDelete.addActionListener(e -> deleteProduct());
        btnClear.addActionListener(e -> clearFields());
        btnLogout.addActionListener(e -> logout());

        refreshTable();
        return panel;
    }

    private JPanel createPOSPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Product ID:"));
        txtSaleId = new JTextField();
        panel.add(txtSaleId);

        panel.add(new JLabel("Quantity Sold:"));
        txtSaleQty = new JTextField();
        panel.add(txtSaleQty);

        panel.add(new JLabel("Payment Method:"));
        cmbPayment = new JComboBox<>(new String[]{"Cash", "Credit Card"});
        panel.add(cmbPayment);

        panel.add(new JLabel("Total Amount:"));
        lblTotal = new JLabel("RM 0.00");
        panel.add(lblTotal);

        JButton btnCalculate = new JButton("Calculate Total");
        JButton btnPay = new JButton("Process Payment");

        panel.add(btnCalculate);
        panel.add(btnPay);

        btnCalculate.addActionListener(e -> calculateTotal());
        btnPay.addActionListener(e -> processPayment());

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        txtHistoryArea = new JTextArea();
        txtHistoryArea.setEditable(false);

        JButton btnRefresh = new JButton("Refresh Sales History");
        btnRefresh.addActionListener(e -> refreshHistoryArea());

        panel.add(new JScrollPane(txtHistoryArea), BorderLayout.CENTER);
        panel.add(btnRefresh, BorderLayout.SOUTH);

        return panel;
    }

    private void addSampleProducts() {
        productList.add(new Product("P001", "Panadol", 8.50, 50));
        productList.add(new Product("P002", "Vitamin C", 15.00, 30));
        productList.add(new Product("P003", "Cough Syrup", 12.80, 20));
    }

    private void addProduct() {
        try {
            String id = txtId.getText().trim();
            String name = txtName.getText().trim();
            double price = Double.parseDouble(txtPrice.getText().trim());
            int quantity = Integer.parseInt(txtQuantity.getText().trim());

            if (id.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Product ID and Product Name cannot be empty.");
                return;
            }

            if (price <= 0 || quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Price and quantity must be greater than 0.");
                return;
            }

            if (findProduct(id) != null) {
                JOptionPane.showMessageDialog(this, "Product ID already exists.");
                return;
            }

            productList.add(new Product(id, name, price, quantity)); //add item
            saveToFile();// save to a file
            refreshTable();
            clearFields();
            JOptionPane.showMessageDialog(this, "Product added successfully.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please enter valid product details.");
        }
    }

    private void searchProduct() {
        String id = txtId.getText().trim();

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Product ID to search.");
            return;
        }

        Product p = findProduct(id);

        if (p == null) {
            JOptionPane.showMessageDialog(this, "Product not found.");
        } else {
            txtName.setText(p.getName());
            txtPrice.setText(String.valueOf(p.getPrice()));
            txtQuantity.setText(String.valueOf(p.getQuantity()));

            String details = "Product Details\n"
                    + "-------------------------\n"
                    + "Product ID: " + p.getId() + "\n"
                    + "Product Name: " + p.getName() + "\n"
                    + "Price: RM " + String.format("%.2f", p.getPrice()) + "\n"
                    + "Quantity: " + p.getQuantity();

            JOptionPane.showMessageDialog(this, details);
        }
    }

    private void updateProduct() {
        try {
            String id = txtId.getText().trim();
            String name = txtName.getText().trim();
            double price = Double.parseDouble(txtPrice.getText().trim());
            int quantity = Integer.parseInt(txtQuantity.getText().trim());

            if (id.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Product ID and Product Name cannot be empty.");
                return;
            }

            if (price <= 0 || quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Price and quantity must be greater than 0.");
                return;
            }

            Product p = findProduct(id);
            if (p == null) {
                JOptionPane.showMessageDialog(this, "Product not found.");
                return;
            }

            p.setName(name);
            p.setPrice(price);
            p.setQuantity(quantity);

            refreshTable();
            clearFields();
            JOptionPane.showMessageDialog(this, "Product updated successfully.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please enter valid data.");
        }
    }

    private void deleteProduct() {
        String id = txtId.getText().trim();

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Product ID to delete.");
            return;
        }

        Product p = findProduct(id);

        if (p == null) {
            JOptionPane.showMessageDialog(this, "Product not found.");
            return;
        }

        productList.remove(p);
        refreshTable();
        clearFields();
        JOptionPane.showMessageDialog(this, "Product deleted successfully.");
    }

    private void calculateTotal() {
        try {
            String id = txtSaleId.getText().trim();
            int qty = Integer.parseInt(txtSaleQty.getText().trim());

            Product p = findProduct(id);

            if (p == null) {
                JOptionPane.showMessageDialog(this, "Product not found.");
                return;
            }

            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.");
                return;
            }

            if (qty > p.getQuantity()) {
                JOptionPane.showMessageDialog(this, "Insufficient stock.");
                return;
            }

            double total = p.getPrice() * qty;
            lblTotal.setText(String.format("RM %.2f", total));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please enter valid quantity.");
        }
    }

    private void processPayment() {
        try {
            String id = txtSaleId.getText().trim();
            int qty = Integer.parseInt(txtSaleQty.getText().trim());

            Product p = findProduct(id);

            if (p == null) {
                JOptionPane.showMessageDialog(this, "Product not found.");
                return;
            }

            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.");
                return;
            }

            if (qty > p.getQuantity()) {
                JOptionPane.showMessageDialog(this, "Insufficient stock.");
                return;
            }

            double total = p.getPrice() * qty;
            p.setQuantity(p.getQuantity() - qty);

            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            String record = date + "|" + time + "|" + p.getName() + "|" + qty + "|" +
                    String.format("%.2f", total) + "|" + cmbPayment.getSelectedItem();

            salesHistory.add(record);

            refreshTable();
            lblTotal.setText(String.format("RM %.2f", total));
            txtSaleId.setText("");
            txtSaleQty.setText("");

            JOptionPane.showMessageDialog(this, "Payment processed successfully.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please enter valid sale details.");
        }
    }

    private void refreshHistoryArea() {
        txtHistoryArea.setText("");

        String currentDate = "";

        for (String record : salesHistory) {
            String[] parts = record.split("\\|");

            String date = parts[0];
            String time = parts[1];
            String productName = parts[2];
            String qty = parts[3];
            String total = parts[4];
            String payment = parts[5];

            if (!date.equals(currentDate)) {
                currentDate = date;
                txtHistoryArea.append("===== " + date + " =====\n");
            }

            txtHistoryArea.append(time + " | " + productName + " x" + qty +
                    " | RM " + total + " | " + payment + "\n");
        }
    }

    private Product findProduct(String id) {
        for (Product p : productList) {
            if (p.getId().equalsIgnoreCase(id)) {
                return p;
            }
        }
        return null;
    }

    private void refreshTable() {
        tableModel.setRowCount(0);

        for (Product p : productList) {
            tableModel.addRow(new Object[]{
                    p.getId(),
                    p.getName(),
                    String.format("%.2f", p.getPrice()),
                    p.getQuantity()
            });
        }
    }

    private void clearFields() {
        txtId.setText("");
        txtName.setText("");
        txtPrice.setText("");
        txtQuantity.setText("");
    }

    private void logout() {
        new LoginFrame();
        dispose();
    }
}