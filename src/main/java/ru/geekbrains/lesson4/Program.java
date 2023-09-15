package ru.geekbrains.lesson4;

import java.util.*;

public class Program {

/**
 * Разработать контракты и компоненты системы "Покупка онлайн билетов на автобус в час пик".
 *
 * 1.  Предусловия.
 * 2.  Постусловия.
 * 3.  Инвариант.
 * 4.  Определить абстрактные и конкретные классы.
 * 5.  Определить интерфейсы.
 * 6.  Реализовать наследование.
 * 7.  Выявить компоненты.
 * 8.  Разработать Диаграмму компонент использую нотацию UML 2.0. Общая без деталей.
 */
    public static void main(String[] args) throws InterruptedException {

        Core core = new Core();
        core.getCustomerProvider().addCustomer("log","password");
        MobileApp mobileApp = new MobileApp(core.getTicketProvider(), core.getCustomerProvider());
        BusStation busStation = new BusStation(core.getTicketProvider());


        if (mobileApp.buyTicket("11000000221")){
            System.out.println("Клиент успешно купил билет.");
            mobileApp.searchTicket(new Date());
            Collection<Ticket> tickets = mobileApp.getTickets();
            if (busStation.checkTicket(tickets.stream().findFirst().get().getQrcode())){
                System.out.println("Клиент успешно прошел в автобус.");
            }
        }

        core.getCustomerProvider().addCustomer("log","password");


    }

}

class Core{

    private final CustomerProvider customerProvider;
    private final TicketProvider ticketProvider;
    private final PaymentProvider paymentProvider;
    private final Database database;

    public Core(){
        database = new Database();
        customerProvider = new CustomerProvider(database);
        paymentProvider = new PaymentProvider();
        ticketProvider = new TicketProvider(database, paymentProvider);
    }

    public TicketProvider getTicketProvider() {
        return ticketProvider;
    }

    public CustomerProvider getCustomerProvider() {
        return customerProvider;
    }

}


/**
 * Покупатель
 */
class Customer{

    private static int counter;

    private final int id;

    private String login;

    private String password;

    private Collection<Ticket> tickets;

    {
        id = ++counter;
    }

    private Customer(){
    }

    public Customer(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }


    public String getPassword() {
        return password;
    }

    private void setPassword(String password) {
        this.password = password;
    }

    public Collection<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(Collection<Ticket> tickets) {
        this.tickets = tickets;
    }

    public int getId() {
        return id;
    }

}

/**
 * Билет
 */
class Ticket{

    private static int counter;

    private final int id;

    private int customerId;

    private Date date;

    private String qrcode;

    private boolean enable = true;

    public Ticket(int customerId, Date date) {
        id = ++counter;
        this.customerId = customerId;
        this.date = date;
        this.qrcode = "qr";
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getId() {
        return id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public Date getDate() {
        return date;
    }

    public String getQrcode() {
        return qrcode;
    }

    public boolean isEnable() {
        return enable;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", date=" + date +
                ", qrcode='" + qrcode + '\'' +
                ", enable=" + enable +
                '}';
    }
}


/**
 * База данных
 */
class Database{

    private static int counter;
    private Collection<Ticket> tickets = new ArrayList<>();
    private Collection<Customer> customers = new ArrayList<>();

    public Database() {
        tickets.add(new Ticket(3,new Date()));
        tickets.add(new Ticket(2,new Date()));
        tickets.add(new Ticket(4,new Date()));
    }

    public Collection<Ticket> getTickets() {
        return tickets;
    }

    public Collection<Customer> getCustomers() {
        return customers;
    }

    public void addCustomer(Customer customer){
        customers.add(customer);
    }

    public void addTicket(Ticket ticket){
        tickets.add(ticket);
    }


    /**
     * Получить актуальную стоимость билета
     * @return
     */
    public double getTicketAmount(){
        return 45;
    }

    /**
     * Получить идентификатор заявки на покупку билета
     * @return
     */
    public int createTicketOrder(int clientId){
        return ++counter;
    }

}

class PaymentProvider{

    public boolean buyTicket(int orderId, String cardNo, double amount){
        //TODO: Обращение к платежному шлюзу, попытка выполнить списание средств ...
        return true;
    }

}

/**
 * Мобильное приложение
 */
class MobileApp{

    private final Customer customer;
    private final TicketProvider ticketProvider;
    private final CustomerProvider customerProvider;


    public MobileApp(TicketProvider ticketProvider, CustomerProvider customerProvider) {
        this.ticketProvider = ticketProvider;
        this.customerProvider = customerProvider;
        customer = customerProvider.getCustomer("log", "password");

    }

    public Collection<Ticket> getTickets(){
        return customer.getTickets();
    }

    public void searchTicket(Date date){
        customer.setTickets(ticketProvider.searchTicket(customer.getId(), date));
    }

    public boolean buyTicket(String cardNo){
        return ticketProvider.buyTicket(customer.getId(), cardNo);
    }

}

class TicketProvider{

    private final Database database;
    private final PaymentProvider paymentProvider;

    public TicketProvider(Database database, PaymentProvider paymentProvider){
        this.database = database;
        this.paymentProvider = paymentProvider;
    }

    public Collection<Ticket> searchTicket(int clientId, Date date){
        Collection<Ticket> tickets = new ArrayList<>();

        for (Ticket ticket: database.getTickets()) {
            if (ticket.getCustomerId() == clientId && ticket.getDate().equals(date))
                tickets.add(ticket);

        }
        return tickets;

    }

    public boolean buyTicket(int clientId, String cardNo){

        int orderId = database.createTicketOrder(clientId);
        double amount = database.getTicketAmount();
        if(paymentProvider.buyTicket(orderId,  cardNo, amount)){
            database.addTicket(new Ticket(clientId,new Date()));
            return true;
        }
        return false;

    }

    public boolean checkTicket(String qrcode){
        for (Ticket ticket: database.getTickets()) {
            if (ticket.getQrcode().equals(qrcode)){
                ticket.setEnable(false);
                return true;
            }
        }
        return false;
    }
}


class CustomerProvider{

    private Database database;

    public CustomerProvider(Database database) {
        this.database = database;
    }

    /**
     * Добавление клиента в базу данных
     * @param login не короче 3 символов
     * @param password не короче 5 символов
     * @return Customer
     */
    public void addCustomer(String login, String password){
        // Предусловие
        if(!validationData(login,password)) {
            throw new RuntimeException("incorrect input!");
        }

        // Инвариант
        validationDb(database);

        // Постусловие
        if (database.getCustomers().stream().anyMatch(x -> Objects.equals(x.getLogin(), login))) {
            throw new RuntimeException("That name's taken");
        }
        database.addCustomer(new Customer(login,password));
    }


    /**
     * Поиск клиента в бaзе данных
     * @param login не короче 3 символов
     * @param password не короче 5 символов
     * @return Customer
     */
    public Customer getCustomer(String login, String password){
         // Предусловие
        if(!validationData(login,password)) {
            throw new RuntimeException("incorrect input!");
        }

        // Инвариант
         validationDb(database);

        Customer customer = (database.getCustomers()).stream()
                .filter(x -> x.getLogin().equals(login) && x.getPassword().equals(password))
                .findAny()
                .orElse(null);

        // Постусловие
        if (customer == null) {
            throw new RuntimeException("Customer not found");
        }
        return customer;
    }

    private void validationDb(Database database) {
        if (database.getTickets() == null || database.getCustomers() == null){
            throw new RuntimeException("incorrect object state");
        }
    }

    private boolean validationData(String login,String password) {
        return login.length() >= 3 && password.length() >= 5;
    }

}

/**
 * Автобусная станция
 */
class BusStation{

    private final TicketProvider ticketProvider;

    public BusStation(TicketProvider ticketProvider){
        this.ticketProvider = ticketProvider;
    }

    public boolean checkTicket(String qrCode){
        return ticketProvider.checkTicket(qrCode);
    }

}


