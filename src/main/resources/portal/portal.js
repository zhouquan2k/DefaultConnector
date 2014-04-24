/**
 * @class Ext.app.Portal
 * @extends Object
 * A sample portal layout application class.
 */
 
 Ext.define('model.resource.Agent',
{
	extend:'Ext.data.ModelEx',
	fields: ['_className','agentId','userName','agentMode','agentStatus','station','stationStatus','otherSide'
			    // {name:'org',model:'model.ccadmin.Entity.Org',defaultValue:null},
			     //    {name:'roles',model:'model.security.Role',defaultValue:null} 
			     ],
		idProperty:'agentId'	
	});


Ext.define('app.Portlet',{
	extend: 'Ext.panel.Panel',
	//alias:'widget.myportlet',
	
	/*
	constructor: function(config)
	{
		this.config=config;
	},
	*/
	
	initComponent:function(){
	
		this.callParent(arguments);
		
		
		var uiName='test-portlet.ui';
		var params={}
		
		
		global.application.loadUI2(uiName,params,function(uiObj){
    		//global.curController=uiObj;
    		this.updateUI(uiObj,params,this);
    	},this);
	},
		
	updateUI:function(uiObj,params,parent) //move to Util
    {    	
    	//global.params=params||{};
    	var cmp=null;
    	parent.removeAll(true);
    	if (uiObj!=null) //remove only
    	{
    		var ui=uiObj.ui;
    		var controller=uiObj;
    		try
    		{       	
    			cmp=Ext.ComponentMgr.create(ui); //use config create ui component
    		}
    		catch (e)
    		{
    			alert("load ui failed:can't create ext ui component:"+e);
    			this.resetUI();
    			throw e;
    		}
    		if (controller.initController) controller.initController();
    		    		
    		//fireEvent onRefresh,TODO do it after render?
    		controller.fireUIReady();
    	}
    	
        if (cmp) parent.add(cmp);
        else parent.add({xtype:'panel',frame:true});
        parent.doLayout();
        //Ext.getCmp('viewport').setLoading(false);
    }
    
});

var appcfg={
    name: 'PortalApp',
    
    initEx:function()
    {
    	//document.title="Portal";
    },
        
    launch: function() {
    
    	Ext.Loader.setConfig({enabled: true});
    	Ext.BLANK_IMAGE_URL="/connector/images/s.gif";
    	
    	
    	this.remoting=Object.create(Remoting);
    	this.remoting.init({eventUrl:'/connector/ajaxevent',event:true});    	
    	this.remoting.setExceptionHandler(function(t){
    		alert('一般失败：'+t.message);
    	});
    	
    	
    	/*
    	cti.monitorDevice('3331',function(ret,t){
    		alert("success:"+ret);
    	});
    	res.queryResourceTypes(function(resouceTypes){
    		alert(Ext.encode(resouceTypes));    		
    		cti.makeCall({deviceId:'3331',dest:'18075193868'});
    		cti.makeCall({deviceId:'3331',dest:'18075193868'}
    		  ,function(a,t,c,d,e){
    			var isSuccess=!(t.type=="exception");
    			alert(isSuccess?"成功:":"失败:"+t.message);
    			}); 
    	});
    	*/
    	
    	  	    	
    	var provider=this.remoting.rpcRemoting.provider;
    	provider.on('beforecallback',function( provider, event, transaction )
    	{
    		//if (transaction.callback) alert(transaction.method+":"+transaction.callback.length);
    		var ret=true;
    		if (event.type=='exception')
    		{
    			if (event.errCode=='401')
                {
        			global.application.needLogin();
                }
    			else
    			{
    				if (transaction.callback==null||transaction.callback.length<2)
    				{
    					alert(' remote method \"'+transaction.action+'.'+transaction.method+'\" failed: '+event.message);
    					ret=false;
    				}
    			}    			
    		}
    		return ret;
    	});
    	
    	
    	window.setInterval(function(){
	        cti.heartbeat();	        
	    },10000);
    	
        Ext.create('app.Portal');
    }
    
 };


Ext.define('app.Portal', {

    extend: 'Ext.container.Viewport',
    requires: ['Ext.app.PortalPanel', 'Ext.app.PortalColumn', 'Ext.app.GridPortlet', 'Ext.app.ChartPortlet'],

    getTools: function(){
        return [{
            xtype: 'tool',
            type: 'gear',
            handler: function(e, target, header, tool){
                var portlet = header.ownerCt;
                portlet.setLoading('Loading...');
                Ext.defer(function() {
                    portlet.setLoading(false);
                }, 2000);
            }
        }];
    },

    initComponent: function(){
    
    
        var content = '<div class="portlet-content">'+Ext.example.shortBogusMarkup+'</div>';

        Ext.apply(this, {
            id: 'app-viewport',
            layout: {
                type: 'border',
                padding: '0 5 5 5' // pad the layout from the window edges
            },
            items: [{
                id: 'app-header',
                xtype: 'box',
                region: 'north',
                height: 40,
                html: '实时监控'
            },{
                xtype: 'container',
                region: 'center',
                layout: 'border',
                items: [
                	/*
                	{
                    id: 'app-options',
                    title: 'Options',
                    region: 'west',
                    animCollapse: true,
                    width: 200,
                    minWidth: 150,
                    maxWidth: 400,
                    split: true,
                    collapsible: true,
                    layout:{
                        type: 'accordion',
                        animate: true
                    },
                    items: [{
                        html: content,
                        title:'Navigation',
                        autoScroll: true,
                        border: false,
                        iconCls: 'nav'
                    },{
                        title:'Settings',
                        html: content,
                        border: false,
                        autoScroll: true,
                        iconCls: 'settings'
                    }]
                },*/
                {
                    id: 'app-portal',
                    xtype: 'portalpanelex',
                    region: 'center',
                    items: [{
                        id: 'col-1',
                        columnWidth:0.7,
                        items: [{
                            id: 'portlet-1',
                            title: '坐席状态',
                            tools: this.getTools(),
                            items: //Ext.create('Ext.app.GridPortlet'),
                            	Ext.create('app.Portlet'),
                            	//{xtype:'myportlet',config:{id:'portlet-1'}},
                            listeners: {
                                'close': Ext.bind(this.onPortletClose, this)
                            }
                        },{
                            id: 'portlet-2',
                            title: 'Portlet 2',
                            tools: this.getTools(),
                            html: content,
                            listeners: {
                                'close': Ext.bind(this.onPortletClose, this)
                            }
                        }]
                    },
                    /*
                    {
                        id: 'col-2',
                        items: [{
                            id: 'portlet-3',
                            title: 'Portlet 3',
                            tools: this.getTools(),
                            html: '<div class="portlet-content">'+Ext.example.bogusMarkup+'</div>',
                            listeners: {
                                'close': Ext.bind(this.onPortletClose, this)
                            }
                        }]
                    },
                    */
                    {
                        id: 'col-3',
                        columnWidth:0.3,
                        items: [{
                            id: 'portlet-4',
                            title: 'Stock Portlet',
                            tools: this.getTools(),
                            items: Ext.create('Ext.app.ChartPortlet'),
                            listeners: {
                                'close': Ext.bind(this.onPortletClose, this)
                            }
                        }]
                    }]
                }]
            }]
        });
        this.callParent(arguments);
    },

    onPortletClose: function(portlet) {
        this.showMsg('"' + portlet.title + '" was removed');
    },

    showMsg: function(msg) {
        var el = Ext.get('app-msg'),
            msgId = Ext.id();

        this.msgId = msgId;
        el.update(msg).show();

        Ext.defer(this.clearMsg, 3000, this, [msgId]);
    },

    clearMsg: function(msgId) {
        if (msgId === this.msgId) {
            Ext.get('app-msg').hide();
        }
    }
});


Ext.define('Ext.app.PortalPanelEx', {
	extend: 'Ext.app.PortalPanel',
	alias: 'widget.portalpanelex',
	
	beforeLayout: function() {	
		
		var items = this.layout.getLayoutItems(),
            len = items.length,
            firstAndLast = ['x-portal-column-first', 'x-portal-column-last'],
            i, item, last;

        for (i = 0; i < len; i++) {
            item = items[i];
            if (!item.columnWidth) item.columnWidth = 1 / len;
            last = (i == len-1);

            if (!i) { // if (first)
                if (last) {
                    item.addCls(firstAndLast);
                } else {
                    item.addCls('x-portal-column-first');
                    item.removeCls('x-portal-column-last');
                }
            } else if (last) {
                item.addCls('x-portal-column-last');
                item.removeCls('x-portal-column-first');
            } else {
                item.removeCls(firstAndLast);
            }
        }

        
	}
});


Ext.applicationEx(appcfg);