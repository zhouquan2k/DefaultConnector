package app.ctiServer.connector.protocol.udp;

/**
 * Constant variable
 * @author Dev.pyh
 *
 */
public class UDPConstants {
	
	public static final String CTI_VERSION = "3.2.00";
	
	// Request.
	public static final byte REQUEST_MONITOR_DEVICE = 0x22;
	public static final byte REQUEST_STOP_MONITOR_DEVICE = 0x23;
	public static final byte REQUEST_MAKE_CALL = 0x2A;
	public static final byte REQUEST_HEART_BEAT = 0x53;
	public static final byte REQUEST_CHANGE_AGENT_STATE = 0x20;
	public static final byte REQUEST_ANSWER_CALL = 0x26;
	public static final byte REQUEST_CONSULTATION_CALL = 0x2F;
	public static final byte REQUEST_HOLD_CALL = 0x28;
	public static final byte REQUEST_RETRIEVE_CALL = 0x29;
	public static final byte REQUEST_TRANSFER_CALL = 0x2B;
	public static final byte REQUEST_ALTERNATE_CALL = 0x2D;
	public static final byte REQUEST_RECONNECT_CALL = 0x30;
	public static final byte REQUEST_CONFERENCE_CALL = 0x2E;
    public static final byte REQUEST_SINGLE_STEP_CONFERENCE = 0x3B;
    public static final byte REQUEST_SINGLE_STEP_TRANSFER = 0x35;
    public static final byte REQUEST_CLEAR_CALL = 0x27;
    public static final byte REQUEST_DISCONNECT_CALL = 0x34;
    
    public static final byte REQUEST_QUERY_QUEUE_INFO = 0x37;
    public static final byte REQUEST_QUERY_VDN_LIST = 0x4D;
    public static final byte REQUEST_QUERY_TRUNK_INFO_V2 = 0x54;
    public static final byte REQUEST_QUERY_REASONCODE_INFO_V2 =0x55;
    public static final byte REQUEST_QUERY_GROUP_AGENTS_V2 = 0x56;
    
    public static final byte REQUEST_SEND_EMBED_MESSAGES = (byte)0xA0;
    
    public static final byte REQUEST_QUERY_AGENT_STATE_V2 = 0x51;
    public static final byte REQUEST_QUERY_GROUP_INFO_V2 = 0x52;
    
    public static final byte REQUEST_FAILURE = (byte)0x99;
	
	// Events .
	public static final byte EVENT_SERVICE_INITIATED = 0x23;
	public static final byte EVENT_ORIGINATED_CALL = 0x27;
	public static final byte EVENT_ALERTING = 0x24;
	public static final byte EVENT_ESTABLISHED = 0x21;
	public static final byte EVENT_INCOMMING_CALL = 0x20;
	public static final byte EVENT_AGENT_STATE_CHANGED = 0x26;
	public static final byte EVENT_AGENT_STATE_CHANGED_V2 = 0x55;
	public static final byte EVENT_CALL_DISCONNECT = 0x22;
	public static final byte EVENT_HELD_CALL = 0x28;
	public static final byte EVENT_RETRIEVED_CALL = 0x29;
	public static final byte EVENT_TRANSFERED_CALL = 0x2D;
	public static final byte EVENT_CONFERENCE_CALL = 0x2A;
	public static final byte EVENT_DIVERTED_CALL = 0x2C;
	public static final byte EVENT_QUEUED_CALL = 0x51;
	public static final byte EVENT_FAILURE_CALL = 0x25;
	public static final byte EVENT_SERVER_NOTIFY = 0x60;
	public static final byte EVENT_INCOMMING_CALL_V2 = 0x6A;
	public static final byte EVENT_INCOMMING_CALL_V3 = 0x73;
	public static final byte EVENT_ALERTING_V2 = 0x6B;
	public static final byte EVENT_ESTABLISHED_V2 = 0x74;
	
	public static final byte EVENT_CONN_DISCONNECT = 0x2B;
	public static final byte EVENT_CONFERENCE_CALLV2 = 0x6C;
	
	// UDP  
	public static final byte PKG_HEAD = 0x1E;
	public static final byte PKG_TAIL = 0x1F;
	public static final byte PKG_INTERVAL = 0x01;
	
	// Message Type
	public static final byte MSG_REQUEST = 0x30;
	public static final byte MSG_RESPONSE = 0x31;
	public static final byte MSG_EVENT = 0x32;
	
	// Message destination
	public static final byte TYPE_CLIENT = 0x40;
	public static final byte TYPE_SERVER = 0x32;
	
	public static final int REQ_DEVICE_NULL = 201;
	public static final int REQ_CTI_FAILURE = 202;
	public static final int REQ_ERROR_INPUTFORMAT = 203;
	public static final int SESSION_NOT_EXIST = 204;
	public static final int CTI_RES_FAILURE = 205;
	public static final int CALL_NOT_EXIST = 206;
	
	// CSTA Universal Failure 
	public enum CSTAUniversalFailure_t 
	{
	    GENERIC_UNSPECIFIED(129), //坐席已登录
	    GENERIC_OPERATION(128),//分机已登录
	    REQUEST_INCOMPATIBLE_WITH_OBJECT(2) ,
	    VALUE_OUT_OF_RANGE(3) ,
	    OBJECT_NOT_KNOWN(4) ,
	    INVALID_CALLING_DEVICE(5) ,
	    INVALID_CALLED_DEVICE(6) ,
	    INVALID_FORWARDING_DESTINATION(7) ,
	    PRIVILEGE_VIOLATION_ON_SPECIFIED_DEVICE(8) ,
	    PRIVILEGE_VIOLATION_ON_CALLED_DEVICE(9),
	    PRIVILEGE_VIOLATION_ON_CALLING_DEVICE(10),
	    INVALID_CSTA_CALL_IDENTIFIER(11),
	    INVALID_CSTA_DEVICE_IDENTIFIER(004),           
	    INVALID_CSTA_CONNECTION_IDENTIFIER(13),
	    INVALID_DESTINATION(14),
	    INVALID_FEATURE(15),
	    INVALID_ALLOCATION_STATE(16),
	    INVALID_CROSS_REF_ID(17),
	    INVALID_OBJECT_TYPE(18),
	    SECURITY_VIOLATION(19),
	    GENERIC_STATE_INCOMPATIBILITY(21),
	    INVALID_OBJECT_STATE(22),
	    INVALID_CONNECTION_ID_FOR_ACTIVE_CALL(23),
	    NO_ACTIVE_CALL(24),
	    NO_HELD_CALL(25),
	    NO_CALL_TO_CLEAR(26),
	    NO_CONNECTION_TO_CLEAR(27),
	    NO_CALL_TO_ANSWER(28),
	    NO_CALL_TO_COMPLETE(29),    
	    GENERIC_SYSTEM_RESOURCE_AVAILABILITY(31),
	    SERVICE_BUSY(32),
	    RESOURCE_BUSY(133),
	    RESOURCE_OUT_OF_SERVICE(34),
	    NETWORK_BUSY(35),
	    NETWORK_OUT_OF_SERVICE(36),
	    OVERALL_MONITOR_LIMIT_EXCEEDED(37),
	    CONFERENCE_MEMBER_LIMIT_EXCEEDED(38),
	    GENERIC_SUBSCRIBED_RESOURCE_AVAILABILITY(41),
	    OBJECT_MONITOR_LIMIT_EXCEEDED(42),
	    EXTERNAL_TRUNK_LIMIT_EXCEEDED(43),
	    OUTSTANDING_REQUEST_LIMIT_EXCEEDED(44),
	    GENERIC_PERFORMANCE_MANAGEMENT(51),
	    PERFORMANCE_LIMIT_EXCEEDED(52),
	    UNSPECIFIED_SECURITY_ERROR(60),
	    SEQUENCE_NUMBER_VIOLATED(61),
	    TIME_STAMP_VIOLATED(62),
	    PAC_VIOLATED(63),
	    SEAL_VIOLATED(64),
	    GENERIC_UNSPECIFIED_REJECTION(70),
	    GENERIC_OPERATION_REJECTION(71),
	    DUPLICATE_INVOCATION_REJECTION(72),
	    UNRECOGNIZED_OPERATION_REJECTION(73),
	    MISTYPED_ARGUMENT_REJECTION(74),
	    RESOURCE_LIMITATION_REJECTION(75),
	    ACS_HANDLE_TERMINATION_REJECTION(76),
	    SERVICE_TERMINATION_REJECTION(77),
	    REQUEST_TIMEOUT_REJECTION(78),
	    REQUESTS_ON_DEVICE_EXCEEDED_REJECTION(79),
	    UNRECOGNIZED_APDU_REJECTION(80),
	    MISTYPED_APDU_REJECTION(81),
	    BADLY_STRUCTURED_APDU_REJECTION(82),
	    INITIATOR_RELEASING_REJECTION(83),
	    UNRECOGNIZED_LINKEDID_REJECTION(84),//密码错误
	    LINKED_RESPONSE_UNEXPECTED_REJECTION(85),
	    UNEXPECTED_CHILD_OPERATION_REJECTION(86),
	    MISTYPED_RESULT_REJECTION(87),
	    UNRECOGNIZED_ERROR_REJECTION(88),
	    UNEXPECTED_ERROR_REJECTION(89),
	    MISTYPED_PARAMETER_REJECTION(90),
	    NON_STANDARD (100),
	    RSN_INVALID_AGENT_NO (131),
	    RSN_WRONG_AGENT_PASSWORD(132),
	    RSN_INVALID_CONNECTION(137),
	    INIVALID_DEVICE(130);
	    
	    private int value;
	    CSTAUniversalFailure_t(int value)
		{
			this.value=value;
		}
		public int getValue()
		{
			return this.value;
		}
		
		public static int parse(String desc)
		{
			String[] descs = desc.split(" ");
				try {
					return CSTAUniversalFailure_t.valueOf(descs[descs.length-2]).value;
				} catch (Exception e) {
					if(desc.length() > 0 && desc.contains("invalid deviceid"))
						return 130;
					else if (desc.length() > 0 && desc.contains("invalid agent"))
						return 131;
					else if(desc.length() > 0 && desc.contains("invalid connection"))
						return 137;
					else if(desc.length() > 0 && desc.contains("invalid password"))
						return  132;
					else
					    return 0;
				}
			}
		
	} 
	
	public enum EmbedMessageType{
		Notification,Webchat,Mail,SM,IM,Weibo
	}
}
