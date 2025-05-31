package it.unical.trenical.grpc.ticket;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Servizio per la gestione dei biglietti
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.64.0)",
    comments = "Source: ticket_service.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class TicketServiceGrpc {

  private TicketServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "trenical.ticket.TicketService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<it.unical.trenical.grpc.ticket.PurchaseTicketRequest,
      it.unical.trenical.grpc.ticket.PurchaseTicketResponse> getPurchaseTicketMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PurchaseTicket",
      requestType = it.unical.trenical.grpc.ticket.PurchaseTicketRequest.class,
      responseType = it.unical.trenical.grpc.ticket.PurchaseTicketResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<it.unical.trenical.grpc.ticket.PurchaseTicketRequest,
      it.unical.trenical.grpc.ticket.PurchaseTicketResponse> getPurchaseTicketMethod() {
    io.grpc.MethodDescriptor<it.unical.trenical.grpc.ticket.PurchaseTicketRequest, it.unical.trenical.grpc.ticket.PurchaseTicketResponse> getPurchaseTicketMethod;
    if ((getPurchaseTicketMethod = TicketServiceGrpc.getPurchaseTicketMethod) == null) {
      synchronized (TicketServiceGrpc.class) {
        if ((getPurchaseTicketMethod = TicketServiceGrpc.getPurchaseTicketMethod) == null) {
          TicketServiceGrpc.getPurchaseTicketMethod = getPurchaseTicketMethod =
              io.grpc.MethodDescriptor.<it.unical.trenical.grpc.ticket.PurchaseTicketRequest, it.unical.trenical.grpc.ticket.PurchaseTicketResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PurchaseTicket"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.ticket.PurchaseTicketRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.ticket.PurchaseTicketResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TicketServiceMethodDescriptorSupplier("PurchaseTicket"))
              .build();
        }
      }
    }
    return getPurchaseTicketMethod;
  }

  private static volatile io.grpc.MethodDescriptor<it.unical.trenical.grpc.ticket.ModifyTicketRequest,
      it.unical.trenical.grpc.ticket.OperationResponse> getModifyTicketMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ModifyTicket",
      requestType = it.unical.trenical.grpc.ticket.ModifyTicketRequest.class,
      responseType = it.unical.trenical.grpc.ticket.OperationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<it.unical.trenical.grpc.ticket.ModifyTicketRequest,
      it.unical.trenical.grpc.ticket.OperationResponse> getModifyTicketMethod() {
    io.grpc.MethodDescriptor<it.unical.trenical.grpc.ticket.ModifyTicketRequest, it.unical.trenical.grpc.ticket.OperationResponse> getModifyTicketMethod;
    if ((getModifyTicketMethod = TicketServiceGrpc.getModifyTicketMethod) == null) {
      synchronized (TicketServiceGrpc.class) {
        if ((getModifyTicketMethod = TicketServiceGrpc.getModifyTicketMethod) == null) {
          TicketServiceGrpc.getModifyTicketMethod = getModifyTicketMethod =
              io.grpc.MethodDescriptor.<it.unical.trenical.grpc.ticket.ModifyTicketRequest, it.unical.trenical.grpc.ticket.OperationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ModifyTicket"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.ticket.ModifyTicketRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.ticket.OperationResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TicketServiceMethodDescriptorSupplier("ModifyTicket"))
              .build();
        }
      }
    }
    return getModifyTicketMethod;
  }

  private static volatile io.grpc.MethodDescriptor<it.unical.trenical.grpc.ticket.CancelTicketRequest,
      it.unical.trenical.grpc.ticket.OperationResponse> getCancelTicketMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CancelTicket",
      requestType = it.unical.trenical.grpc.ticket.CancelTicketRequest.class,
      responseType = it.unical.trenical.grpc.ticket.OperationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<it.unical.trenical.grpc.ticket.CancelTicketRequest,
      it.unical.trenical.grpc.ticket.OperationResponse> getCancelTicketMethod() {
    io.grpc.MethodDescriptor<it.unical.trenical.grpc.ticket.CancelTicketRequest, it.unical.trenical.grpc.ticket.OperationResponse> getCancelTicketMethod;
    if ((getCancelTicketMethod = TicketServiceGrpc.getCancelTicketMethod) == null) {
      synchronized (TicketServiceGrpc.class) {
        if ((getCancelTicketMethod = TicketServiceGrpc.getCancelTicketMethod) == null) {
          TicketServiceGrpc.getCancelTicketMethod = getCancelTicketMethod =
              io.grpc.MethodDescriptor.<it.unical.trenical.grpc.ticket.CancelTicketRequest, it.unical.trenical.grpc.ticket.OperationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CancelTicket"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.ticket.CancelTicketRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.ticket.OperationResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TicketServiceMethodDescriptorSupplier("CancelTicket"))
              .build();
        }
      }
    }
    return getCancelTicketMethod;
  }

  private static volatile io.grpc.MethodDescriptor<it.unical.trenical.grpc.ticket.GetTicketRequest,
      it.unical.trenical.grpc.ticket.GetTicketResponse> getGetTicketMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTicket",
      requestType = it.unical.trenical.grpc.ticket.GetTicketRequest.class,
      responseType = it.unical.trenical.grpc.ticket.GetTicketResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<it.unical.trenical.grpc.ticket.GetTicketRequest,
      it.unical.trenical.grpc.ticket.GetTicketResponse> getGetTicketMethod() {
    io.grpc.MethodDescriptor<it.unical.trenical.grpc.ticket.GetTicketRequest, it.unical.trenical.grpc.ticket.GetTicketResponse> getGetTicketMethod;
    if ((getGetTicketMethod = TicketServiceGrpc.getGetTicketMethod) == null) {
      synchronized (TicketServiceGrpc.class) {
        if ((getGetTicketMethod = TicketServiceGrpc.getGetTicketMethod) == null) {
          TicketServiceGrpc.getGetTicketMethod = getGetTicketMethod =
              io.grpc.MethodDescriptor.<it.unical.trenical.grpc.ticket.GetTicketRequest, it.unical.trenical.grpc.ticket.GetTicketResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTicket"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.ticket.GetTicketRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.ticket.GetTicketResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TicketServiceMethodDescriptorSupplier("GetTicket"))
              .build();
        }
      }
    }
    return getGetTicketMethod;
  }

  private static volatile io.grpc.MethodDescriptor<it.unical.trenical.grpc.ticket.ListTicketsRequest,
      it.unical.trenical.grpc.ticket.ListTicketsResponse> getListTicketsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListTickets",
      requestType = it.unical.trenical.grpc.ticket.ListTicketsRequest.class,
      responseType = it.unical.trenical.grpc.ticket.ListTicketsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<it.unical.trenical.grpc.ticket.ListTicketsRequest,
      it.unical.trenical.grpc.ticket.ListTicketsResponse> getListTicketsMethod() {
    io.grpc.MethodDescriptor<it.unical.trenical.grpc.ticket.ListTicketsRequest, it.unical.trenical.grpc.ticket.ListTicketsResponse> getListTicketsMethod;
    if ((getListTicketsMethod = TicketServiceGrpc.getListTicketsMethod) == null) {
      synchronized (TicketServiceGrpc.class) {
        if ((getListTicketsMethod = TicketServiceGrpc.getListTicketsMethod) == null) {
          TicketServiceGrpc.getListTicketsMethod = getListTicketsMethod =
              io.grpc.MethodDescriptor.<it.unical.trenical.grpc.ticket.ListTicketsRequest, it.unical.trenical.grpc.ticket.ListTicketsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListTickets"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.ticket.ListTicketsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.ticket.ListTicketsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TicketServiceMethodDescriptorSupplier("ListTickets"))
              .build();
        }
      }
    }
    return getListTicketsMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static TicketServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TicketServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TicketServiceStub>() {
        @java.lang.Override
        public TicketServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TicketServiceStub(channel, callOptions);
        }
      };
    return TicketServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static TicketServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TicketServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TicketServiceBlockingStub>() {
        @java.lang.Override
        public TicketServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TicketServiceBlockingStub(channel, callOptions);
        }
      };
    return TicketServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static TicketServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TicketServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TicketServiceFutureStub>() {
        @java.lang.Override
        public TicketServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TicketServiceFutureStub(channel, callOptions);
        }
      };
    return TicketServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Servizio per la gestione dei biglietti
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * Acquisto biglietti
     * </pre>
     */
    default void purchaseTicket(it.unical.trenical.grpc.ticket.PurchaseTicketRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.ticket.PurchaseTicketResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPurchaseTicketMethod(), responseObserver);
    }

    /**
     * <pre>
     * Modifica biglietto
     * </pre>
     */
    default void modifyTicket(it.unical.trenical.grpc.ticket.ModifyTicketRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.ticket.OperationResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getModifyTicketMethod(), responseObserver);
    }

    /**
     * <pre>
     * Annulla biglietto
     * </pre>
     */
    default void cancelTicket(it.unical.trenical.grpc.ticket.CancelTicketRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.ticket.OperationResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCancelTicketMethod(), responseObserver);
    }

    /**
     * <pre>
     * Ottieni biglietto per ID
     * </pre>
     */
    default void getTicket(it.unical.trenical.grpc.ticket.GetTicketRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.ticket.GetTicketResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTicketMethod(), responseObserver);
    }

    /**
     * <pre>
     * Lista biglietti per utente
     * </pre>
     */
    default void listTickets(it.unical.trenical.grpc.ticket.ListTicketsRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.ticket.ListTicketsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListTicketsMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service TicketService.
   * <pre>
   * Servizio per la gestione dei biglietti
   * </pre>
   */
  public static abstract class TicketServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return TicketServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service TicketService.
   * <pre>
   * Servizio per la gestione dei biglietti
   * </pre>
   */
  public static final class TicketServiceStub
      extends io.grpc.stub.AbstractAsyncStub<TicketServiceStub> {
    private TicketServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TicketServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TicketServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Acquisto biglietti
     * </pre>
     */
    public void purchaseTicket(it.unical.trenical.grpc.ticket.PurchaseTicketRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.ticket.PurchaseTicketResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPurchaseTicketMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Modifica biglietto
     * </pre>
     */
    public void modifyTicket(it.unical.trenical.grpc.ticket.ModifyTicketRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.ticket.OperationResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getModifyTicketMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Annulla biglietto
     * </pre>
     */
    public void cancelTicket(it.unical.trenical.grpc.ticket.CancelTicketRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.ticket.OperationResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCancelTicketMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Ottieni biglietto per ID
     * </pre>
     */
    public void getTicket(it.unical.trenical.grpc.ticket.GetTicketRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.ticket.GetTicketResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTicketMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Lista biglietti per utente
     * </pre>
     */
    public void listTickets(it.unical.trenical.grpc.ticket.ListTicketsRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.ticket.ListTicketsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListTicketsMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service TicketService.
   * <pre>
   * Servizio per la gestione dei biglietti
   * </pre>
   */
  public static final class TicketServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<TicketServiceBlockingStub> {
    private TicketServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TicketServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TicketServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Acquisto biglietti
     * </pre>
     */
    public it.unical.trenical.grpc.ticket.PurchaseTicketResponse purchaseTicket(it.unical.trenical.grpc.ticket.PurchaseTicketRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPurchaseTicketMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Modifica biglietto
     * </pre>
     */
    public it.unical.trenical.grpc.ticket.OperationResponse modifyTicket(it.unical.trenical.grpc.ticket.ModifyTicketRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getModifyTicketMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Annulla biglietto
     * </pre>
     */
    public it.unical.trenical.grpc.ticket.OperationResponse cancelTicket(it.unical.trenical.grpc.ticket.CancelTicketRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCancelTicketMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Ottieni biglietto per ID
     * </pre>
     */
    public it.unical.trenical.grpc.ticket.GetTicketResponse getTicket(it.unical.trenical.grpc.ticket.GetTicketRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTicketMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Lista biglietti per utente
     * </pre>
     */
    public it.unical.trenical.grpc.ticket.ListTicketsResponse listTickets(it.unical.trenical.grpc.ticket.ListTicketsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListTicketsMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service TicketService.
   * <pre>
   * Servizio per la gestione dei biglietti
   * </pre>
   */
  public static final class TicketServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<TicketServiceFutureStub> {
    private TicketServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TicketServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TicketServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Acquisto biglietti
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.ticket.PurchaseTicketResponse> purchaseTicket(
        it.unical.trenical.grpc.ticket.PurchaseTicketRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPurchaseTicketMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Modifica biglietto
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.ticket.OperationResponse> modifyTicket(
        it.unical.trenical.grpc.ticket.ModifyTicketRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getModifyTicketMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Annulla biglietto
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.ticket.OperationResponse> cancelTicket(
        it.unical.trenical.grpc.ticket.CancelTicketRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCancelTicketMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Ottieni biglietto per ID
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.ticket.GetTicketResponse> getTicket(
        it.unical.trenical.grpc.ticket.GetTicketRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTicketMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Lista biglietti per utente
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.ticket.ListTicketsResponse> listTickets(
        it.unical.trenical.grpc.ticket.ListTicketsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListTicketsMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_PURCHASE_TICKET = 0;
  private static final int METHODID_MODIFY_TICKET = 1;
  private static final int METHODID_CANCEL_TICKET = 2;
  private static final int METHODID_GET_TICKET = 3;
  private static final int METHODID_LIST_TICKETS = 4;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_PURCHASE_TICKET:
          serviceImpl.purchaseTicket((it.unical.trenical.grpc.ticket.PurchaseTicketRequest) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.ticket.PurchaseTicketResponse>) responseObserver);
          break;
        case METHODID_MODIFY_TICKET:
          serviceImpl.modifyTicket((it.unical.trenical.grpc.ticket.ModifyTicketRequest) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.ticket.OperationResponse>) responseObserver);
          break;
        case METHODID_CANCEL_TICKET:
          serviceImpl.cancelTicket((it.unical.trenical.grpc.ticket.CancelTicketRequest) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.ticket.OperationResponse>) responseObserver);
          break;
        case METHODID_GET_TICKET:
          serviceImpl.getTicket((it.unical.trenical.grpc.ticket.GetTicketRequest) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.ticket.GetTicketResponse>) responseObserver);
          break;
        case METHODID_LIST_TICKETS:
          serviceImpl.listTickets((it.unical.trenical.grpc.ticket.ListTicketsRequest) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.ticket.ListTicketsResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getPurchaseTicketMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              it.unical.trenical.grpc.ticket.PurchaseTicketRequest,
              it.unical.trenical.grpc.ticket.PurchaseTicketResponse>(
                service, METHODID_PURCHASE_TICKET)))
        .addMethod(
          getModifyTicketMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              it.unical.trenical.grpc.ticket.ModifyTicketRequest,
              it.unical.trenical.grpc.ticket.OperationResponse>(
                service, METHODID_MODIFY_TICKET)))
        .addMethod(
          getCancelTicketMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              it.unical.trenical.grpc.ticket.CancelTicketRequest,
              it.unical.trenical.grpc.ticket.OperationResponse>(
                service, METHODID_CANCEL_TICKET)))
        .addMethod(
          getGetTicketMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              it.unical.trenical.grpc.ticket.GetTicketRequest,
              it.unical.trenical.grpc.ticket.GetTicketResponse>(
                service, METHODID_GET_TICKET)))
        .addMethod(
          getListTicketsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              it.unical.trenical.grpc.ticket.ListTicketsRequest,
              it.unical.trenical.grpc.ticket.ListTicketsResponse>(
                service, METHODID_LIST_TICKETS)))
        .build();
  }

  private static abstract class TicketServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    TicketServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return it.unical.trenical.grpc.ticket.TicketServiceProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("TicketService");
    }
  }

  private static final class TicketServiceFileDescriptorSupplier
      extends TicketServiceBaseDescriptorSupplier {
    TicketServiceFileDescriptorSupplier() {}
  }

  private static final class TicketServiceMethodDescriptorSupplier
      extends TicketServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    TicketServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (TicketServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new TicketServiceFileDescriptorSupplier())
              .addMethod(getPurchaseTicketMethod())
              .addMethod(getModifyTicketMethod())
              .addMethod(getCancelTicketMethod())
              .addMethod(getGetTicketMethod())
              .addMethod(getListTicketsMethod())
              .build();
        }
      }
    }
    return result;
  }
}
