package calculator.client;

import com.proto.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CalculatorClient {
    private static void doSum(ManagedChannel channel) {
        System.out.println("Enter doSum");
        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);
        SumResponse response = stub.sum(SumRequest.newBuilder().setFirstNumber(1).setSecondNumber(1).build());

        System.out.println("Sum 1 + 1 = " + response.getResult());
    }

    private static void doPrimes(ManagedChannel channel) {
        System.out.println("Enter doPrimes");
        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);

        stub.primes(PrimeRequest.newBuilder().setNumber(567890).build()).forEachRemaining(response ->
            System.out.println(response.getPrimeFactor())
        );
    }

    private static void doAvg(ManagedChannel channel) throws InterruptedException {
        System.out.println("Enter doAvg");
        CalculatorServiceGrpc.CalculatorServiceStub stub = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<AvgRequest> stream = stub.avg(new StreamObserver<>() {
            @Override
            public void onNext(AvgResponse response) {
                System.out.println("Avg = " + response.getResult());
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).forEach(number ->
            stream.onNext(AvgRequest.newBuilder().setNumber(number).build())
        );

        stream.onCompleted();
        latch.await();
    }

    private static void doMax(ManagedChannel channel) throws InterruptedException {
        System.out.println("Enter doMax");
        CalculatorServiceGrpc.CalculatorServiceStub stub = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<MaxRequest> stream = stub.max(new StreamObserver<>() {
            @Override
            public void onNext(MaxResponse response) {
                System.out.println(response.getMax());
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        Arrays.asList(1, 5, 3, 6, 2, 20).forEach(number ->
            stream.onNext(MaxRequest.newBuilder().setNumber(number).build())
        );

        stream.onCompleted();

        latch.await(3, TimeUnit.SECONDS);
    }

    private static void doSqrt(ManagedChannel channel) {
        System.out.println("Enter doSqrt");
        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);
        SqrtResponse response = stub.sqrt(SqrtRequest.newBuilder().setNumber(-1).build());

        System.out.println("Sqrt -1 = " + response.getResult());
    }

    public static void main(String[] args) throws InterruptedException {
        if (args.length == 0) {
            System.out.println("Need one argument to work");
        }

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052).usePlaintext().build();

        switch (args[0]) {
            case "sum" -> doSum(channel);
            case "primes" -> doPrimes(channel);
            case "avg" -> doAvg(channel);
            case "max" -> doMax(channel);
            case "sqrt" -> doSqrt(channel);
            default -> System.out.println("Keyword Invalid: " + args[0]);
        }

        System.out.println("Shutting Down");
        channel.shutdown();
    }
}