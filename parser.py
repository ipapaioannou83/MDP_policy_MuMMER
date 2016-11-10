import json
import time, sys
import random
import qi
import argparse
from naoqi import ALProxy
from naoqi import ALBroker
from naoqi import ALModule
from ShopList import ShopList
from rivescript import RiveScript
from State import state

some_state = None
memory = None
chatbot = None
nextAction = None
userStopedTalking = False
global actionName
global shopList

# State attributes
tskC = False
tskF = False
prevA = 0
dist = 1
ctx = ''
turn = False #0 = agent, 1 = user
usrEng = True
mode = False #0 = task, 1 = chat
timeout = False
usrTerm = False
bye = False
usrEngC = False
lowConf = False

lastUsrInput = ''

# Action dictionary
actionID = {"taskConsume": 1, "Greet": 2, "Goodbye": 3, "Chat": 4, "giveDirections": 5, "wait": 6, "confirm": 7, "requestTask": 8}




#### Action selection function
def action(state):
    bestAction = []
    s = json.dumps(state.__dict__, sort_keys=True)
    print s
    with open('jsonDump.json') as data_file:
        data = json.load(data_file)

    for v in data.values():
             if json.dumps(v['s']['s'], sort_keys=True) == s:
                 maxQ = v['qEntry'][0]['q'] #set the first q as max
                 for action in v['qEntry']:
                     if action['q'] == maxQ:
                         bestAction.append(action['a'])
                     if action['q'] > maxQ:
                        maxQ = action['q']
                        bestAction = []
                        bestAction.append(action['a'])
    print "actions" , len(bestAction)
    try:
        return random.choice(bestAction)['name']
    except IndexError:
        pass
    #return 'Chat'


class SpeechEventModule(ALModule):
    """ A simple module able to react to facedetection events """
    def __init__(self, name):
        ALModule.__init__(self, name)
        self.tts = ALProxy("ALAnimatedSpeech")


    def onSpeechDetected(self, *_args):
        #print ALMemory.getData("Dialog/LastInput")
        # unsubscribe to stop listening. Will resubscribe during user's turn (TODO: probably will change...)
        global memory
        memory.unsubscribeToEvent("Dialog/LastInput", "SpeechEvent")
        ALDialog.unsubscribe('my_dialog_example')
        
        #flipTurn()
        
        global userStopedTalking        
        userStopedTalking = True
        
       # observeState()

class HumanGreeterModule(ALModule):
    """ A simple module able to react
    to facedetection events

    """
    def __init__(self, name):
        ALModule.__init__(self, name)
        self.tts = ALProxy("ALTextToSpeech")


    def onFaceDetected(self, *_args):
        """ This will be called each time a face is
        detected.

        """

        ALTracker.registerTarget("People", ALMemory.getData("PeoplePerception/PeopleDetected")[1][0])
        ALTracker.track("People")
        
        
        # Unsubscribe to the event when talking,
        # to avoid repetitions
        global memory
        ALDialog.subscribe('my_dialog_example')
        #memory.subscribeToEvent("Dialog/LastInput", "SpeechEvent", "onSpeechDetected")
        #memory.unsubscribeToEvent("FaceDetected", "HumanGreeter")
        memory.unsubscribeToEvent("PeoplePerception/PeopleDetected", "HumanGreeter")        
        
        #instantiateMemory()
        #readMemoryState(ALMemory)
        observeState()
        #decodeAction(nextAction)
        
        #memory.subscribeToEvent("FaceDetected", "HumanGreeter", "onFaceDetected")

class EngagementZoneModule(ALModule):
    """ A simple module able to react to facedetection events """
    def __init__(self, name):
        ALModule.__init__(self, name)
        #self.tts = ALProxy("ALTextToSpeech")
        self.tts = ALProxy("ALAnimatedSpeech")

    def onMoveAway(self, *_args):
        print "moved away"
        global dist
        global memory
        memory.unsubscribeToEvent("EngagementZones/PersonApproached", "EngagementZone")  
       # dist = 2
        memory.subscribeToEvent("EngagementZones/PersonApproached", "EngagementZone", "onMoveCloser")
        
    def onMoveCloser(self, *_args):
        print "moved closer"
        global dist
        global memory
        memory.unsubscribeToEvent("EngagementZones/PersonApproached", "EngagementZone")  
        dist = 1
        memory.subscribeToEvent("EngagementZones/PersonMovedAway", "EngagementZone", "onMoveAway")
        

# Read memory variables (state attributes)
def readMemoryState(ALMemory):
    global tskC
    global tskF
    global prevA
    global dist
    global ctx
    global turn
    global usrEng
    global mode
    global timeout
    global usrTerm
    global bye
    global usrEngC
    global lowConf
    global lastUsrInput
    global distance
    
    tskC = str2bool(ALMemory.getData("tskCompleted"))
    tskF = str2bool(ALMemory.getData("tskFilled"))
    prevA = ALMemory.getData("prevAct") # int
    dist = getDistance()
    ctx = ALMemory.getData("ctxTask") # string
    turn = str2bool(ALMemory.getData("turntaking"))
    usrEng = str2bool(ALMemory.getData("usrEngaged"))
    mode = str2bool(ALMemory.getData("mode"))
    timeout = str2bool(ALMemory.getData("timeout"))
    usrTerm = str2bool(ALMemory.getData("usrTermination"))
    bye = str2bool(ALMemory.getData("bye"))
    usrEngC = str2bool(ALMemory.getData("usrEngChat"))
    lowConf = str2bool(ALMemory.getData("lowConf"))
    lastUsrInput = ALMemory.getData("Dialog/LastInput")

    
    #print "memory", tskC, tskF, prevA, dist, ctx, usrEng, mode, timeout, usrTerm, bye, usrEngC, lowConf, turn, lastUsrInput
    
def instantiateMemory():  
    ALMemory.insertData("ctxTask","")
    ALMemory.insertData("shopName","")
    ALMemory.insertData("tskCompleted","False")
    ALMemory.insertData("prevAct", 0)
    #ALMemory.insertData("distance", 1)
    ALMemory.insertData("turntaking","False")
    ALMemory.insertData("usrEngaged","True")
    ALMemory.insertData("mode","False")
    ALMemory.insertData("timeout","False")
    ALMemory.insertData("usrTermination","False")
    ALMemory.insertData("bye","False")
    ALMemory.insertData("usrEngChat","False")
    ALMemory.insertData("lowConf","False")
    ALMemory.insertData("tskFilled","False")
    #ALMemory.insertData("Dialog/LastInput","")
    
    readMemoryState(ALMemory)

    # Initialize chatbot
    global chatbot
    chatbot = RiveScript()
    chatbot.load_directory("/media/elveleg/Data/MuMMER Project/chatbot/eg/brain")
    chatbot.sort_replies()
    
    #print chatbot.reply("localuser", "Do you remember me?")
    

def observeState():
    global some_state
    some_state = generateState()
    some_state.printState()
    
    global nextAction
    nextAction = action(some_state)
    print "Action selected: ", nextAction, '\n'
    
    decodeAction(nextAction)
      
    
def decodeAction(nextAction):
    
    try:
        if nextAction == "Greet":
            greet()
        elif nextAction == "Chat":
            chat(lastUsrInput)
        elif nextAction == "wait":
            wait()
        elif nextAction == "taskConsume":
            taskConsume()
        elif nextAction == "giveDirections":
            giveDirections()
        elif nextAction == "Goodbye":
            goodbye()
        elif nextAction == "confirm":
            confirm()
        elif nextAction == "requestTask":
            requestTask()
        
        if not turn:
            if nextAction == "Chat":
                ALMemory.insertData("mode","True")
            else:
                ALMemory.insertData("mode","False")        
            
        # Write action taken to state
        if actionID[nextAction] != 6:
            ALMemory.insertData("prevAct", actionID[nextAction])
    except KeyError:
        print "ERROR"
        sys.exit(0)
    #TODO asdf
        #decodeAction("Chat") #Set Chat as the default action if state was messed up and not present in the policy
    
    flipTurn()
    observeState()
        
      
def flipTurn():
    if nextAction != None:
        global turn
        turn = not turn
        ALMemory.insertData("turntaking", bool2str(turn))


def chat(sentence):
    print sentence
    global chatbot
    tts = ALProxy("ALAnimatedSpeech")
    try:
        tts.say(str(chatbot.reply("localuser",str(sentence))))
    except RuntimeError:
        print "error in chatbot"


def greet():
    tts = ALProxy("ALAnimatedSpeech")
    tts.say("Hi")
       
       
def goodbye():
    tts = ALProxy("ALAnimatedSpeech")
    tts.say("Have a nice day")
    
    # TODO: Have to change it to engage other person instead of exit
    ALDialog.unsubscribe('my_dialog_example')
    ALTracker.unregisterAllTargets()
    ALTracker.stopTracker()
    myBroker.shutdown()
    sys.exit(0)
    ###################
    
    global memory
    memory.subscribeToEvent("FaceDetected", "HumanGreeter", "onFaceDetected")
    
    instantiateMemory()
    observeState()
    

def confirm():
    tts = ALProxy("ALAnimatedSpeech")
    if lastUsrInput is not None:
        tts.say("Sorry, did you say " + lastUsrInput + "?")
    else:
        tts.say("Sorry, can you repeat that please?") 
       
def giveDirections():
    print "shopName: ", ALMemory.getData("shopName")
    tts = ALProxy("ALAnimatedSpeech")
    tts.say("You are asking for a " + shopList.getShop(ALMemory.getData("shopName")).getCategory() + "shop. " + 
            shopList.getDirections(ALMemory.getData("shopName")))
    ALMemory.insertData("ctxTask","")
    ALMemory.insertData("tskFilled","False")
    ALMemory.insertData("tskCompleted","True")
    
    
def taskConsume():
    tts = ALProxy("ALAnimatedSpeech") #TODO: ("ALAnimatedSpeech") to make it move at the same time. need to find a way to rest it afterwards
    tts.say("Let me see. There are " + str(len( shopList.filteredCategory(ALMemory.getData("ctxTask")) )) +
    " " + ALMemory.getData("ctxTask") + " shops nearby")
    
    print shopList.filteredCategory(ALMemory.getData("ctxTask")).enumShops() 
    tts.say("These are " + shopList.filteredCategory(ALMemory.getData("ctxTask")).enumShops() + ".")

    ALMemory.insertData("ctxTask","")
    ALMemory.insertData("tskFilled","False")
    ALMemory.insertData("tskCompleted","True")
    
def requestTask():
    tts = ALProxy("ALAnimatedSpeech")
    tts.say("Is there anything I can help you with?")
    
    
def wait():
    global memory
    global userStopedTalking
    if not tskF:
        ALMemory.insertData("usrEngChat","True")
        
    ALMemory.insertData("tskCompleted","False")
    ALMemory.insertData("timeout","False")
#    memory.subscribeToEvent("EngagementZones/PersonMovedAway", "EngagementZone", "onMoveAway")
#    memory.subscribeToEvent("EngagementZones/PersonApproached", "EngagementZone", "onMoveCloser")
    memory.subscribeToEvent("Dialog/LastInput", "SpeechEvent", "onSpeechDetected")
    ALDialog.subscribe('my_dialog_example')
    while True:
        if userStopedTalking:
            print "user stoped talking"
            userStopedTalking = False
            break
        
        if (time.time() > time.time() + 10) or (dist == 2):
            print "timeout/walked away"
            break

def generateState():
    readMemoryState(ALMemory)
    some_state = state(tskC, tskF, prevA, dist, ctx, usrEng, mode, timeout, usrTerm, bye, usrEngC, lowConf, turn)
    return some_state
    
def str2bool(s):
    if s == 'True':
         return True
    elif s == 'False':
         return False
    else:
         raise ValueError("Cannot convert {} to a bool".format(s))
        
def bool2str(s):
    if s == 1:
         return 'True'
    elif s == 0:
         return 'False'
    else:
         raise ValueError("Cannot convert {} to a bool".format(s))
        
def getDistance():
    distance = 0
    data = ALMemory.getData("PeoplePerception/PeopleDetected")
    print "distance: ", data[1][0][1]
    try:
        if data[1][0][1] <= 1:
            distance = 1
        if data[1][0][1] > 1 and data[1][0][1] <= 2.5:
            distance = 1
        elif data[1][0][1] > 2.5:
            distance = 2
            ALMemory.insertData("timeout","False")
            print "far, far away"
            
    except TypeError:
        distance = 1        
        
    return distance
    
#tskC, tskF, prevA, dist, ctx, usrEng, mode, timeout, usrTerm, bye, usrEngC, lowConf, turn'''
some_state = state(False, True, 2, 1, 'directions', True, False, False, False, False, False, False, False)
some_state.printState()
print action(some_state)

# Populate the shop list from file
shopList= ShopList()


parser = argparse.ArgumentParser()
parser.add_argument("--ip", type=str, default="192.168.2.5",
                        help="Robot's IP address. If on a robot or a local Naoqi")
parser.add_argument("--port", type=int, default=9559,
                        help="port number, the default value is OK in most cases")
parser.add_argument("--topic-path", type=str, required=False,
                        help="absolute path of the dialog topic file (on the robot)")

args = parser.parse_args()
session = qi.Session()

try:
    session.connect("tcp://{}:{}".format(args.ip, args.port))
    myBroker = ALBroker("myBroker",
       "0.0.0.0",   # listen to anyone
       0,           # find a free port and use it
       args.ip,         # parent broker IP
       args.port)       # parent broker port
    print "Success!"
except RuntimeError:
        print ("\nCan't connect to Naoqi at IP {} (port {}).\nPlease check your script's arguments."
               " Run with -h option for help.\n".format(args.ip, args.port))
        sys.exit(1)


ALSpeechRecognition = session.service("ALSpeechRecognition")
ALMemory = session.service("ALMemory")
ALDialog = session.service("ALDialog")
ALTracker = session.service("ALTracker")
ALEngagementZones = session.service("ALEngagementZones")
ALMotion = session.service("ALMotion")

ALDialog.setLanguage("English")
ALMotion.setBreathEnabled('Arms', True)



topic_content = ('topic: ~example_topic_content()\n'
                       'language: enu\n'
                       'concept:(bye) [bye goodbye cheers]\n'
                       'concept:(coffee) [coffee cappucino latte esspresso americano]\n'
                       'concept:(shop) [starbucks costa public "harware electronics" tesco primark "phone heaven"]\n'
                       'concept:(electronics) [iPhone Samsung case adapter television TV charger mobile phone]\n'
                       'concept:(clothing) [shoes jacket "t-shirt" belt jeans trousers shirt suit caot underwear clothing]\n'
                       #'u: ([e:FrontTactilTouched e:MiddleTactilTouched e:RearTactilTouched]) $tskFilled = True $ctxTask = directions $shopName = costa\n'
                       'u: (Open the Pod bay doors) I am sorry Dave, I am afraid I can not do that.\n'
                       'u: (* ~coffee) $tskFilled=True $ctxTask=coffee $usrEngChat=False\n'
                       'u: (* ~electronics) $tskFilled=True $ctxTask=electronics $usrEngChat=False\n'
                       'u: (* ~clothing) $tskFilled=True $ctxTask=clothing $usrEngChat=False\n'
                       'u: (* ~bye) $bye=True $usrEngChat=False\n'
                       'u: (e:Dialog/NotUnderstood) $usrEngChat=True \n'
                       'u: (_*) $Dialog/LastInput=$1 \n'
                       #'u: (e:Dialog/NotSpeaking5) $timeout=True \n'
                       'u: (* _~shop) $tskFilled=True $ctxTask=directions $shopName=$1 $usrEngChat=False \n') 


# Loading the topics directly as text strings
topic_name = ALDialog.loadTopicContent(topic_content)

# Activating the loaded topics
ALDialog.activateTopic(topic_name)

#ALDialog.subscribe('my_dialog_example')

#print "shopName", ALMemory.getData("shopName"), '\n'

# Reset some state variables in ALMemory
#resetAttributes()

global SpeechEvent
SpeechEvent = SpeechEventModule("SpeechEvent")

global HumanGreeter
HumanGreeter = HumanGreeterModule("HumanGreeter")

global EngagementZone
EngagementZone = EngagementZoneModule("EngagementZone")

# Subscribe to the speech and face detection events:
memory = ALProxy("ALMemory")
pplperc = ALProxy("ALPeoplePerception")
pplperc.subscribe("test")
#memory.subscribeToEvent("Dialog/LastInput", "SpeechEvent", "onSpeechDetected")
memory.subscribeToEvent("PeoplePerception/PeopleDetected", "HumanGreeter", "onFaceDetected")
#memory.subscribeToEvent("EngagementZones/PersonMovedAway", "EngagementZone", "onMoveAway")
#memory.subscribeToEvent("EngagementZones/PersonApproached", "EngagementZone", "onMoveCloser")


instantiateMemory()


try:
    while True:
        time.sleep(1)
finally:
    print
    print "Interrupted by user, shutting down..."
    ALTracker.stopTracker()
    ALTracker.unregisterAllTargets()
    myBroker.shutdown()
    ALMotion.setBreathEnabled('Body', False)
    sys.exit(0)

