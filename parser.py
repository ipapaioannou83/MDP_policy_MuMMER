import json
import time, sys
import random
import qi
import argparse
from naoqi import ALProxy
from naoqi import ALBroker
from naoqi import ALModule
from ShopList import ShopList
from chatterbot import ChatBot

some_state = None
memory = None
chatbot = None
nextAction = None
global actionName
global shopList

# State attributes
tskC = False
tskF = False
prevA = 0
dist = 1
ctx = ''
turn = False
usrEng = True
mode = False
timeout = False
usrTerm = False
bye = False
usrEngC = False
lowConf = False

lastUsrInput = ''

# Action dictionary
actionID = {"taskConsume": 1, "Greet": 2, "Goodbye": 3, "Chat": 4, "giveDirections": 5, "wait": 6, "confirm": 7, "reg_task": 8}


class state:
    def __init__(self, tskC, tskF, prevA, dist, ctx, usrEng, mode, timeout, usrTerm, bye, usrEngC, lowConf, turn):
        self.tskCompleted = tskC
        self.tskFilled = tskF
        self.prevAct = prevA
        self.distance = dist
        self.ctxTask = ctx
        self.turnTaking = turn
        self.usrEngaged = usrEng
        self.mode = mode
        self.timeout = timeout
        self.usrTermination = usrTerm
        self.bye = bye
        self.usrEngChat = usrEngC
        self.lowConf = lowConf

    def printState(self):
        print "State: ", self.tskCompleted, self.tskFilled, self.prevAct, self.distance, self.ctxTask, self.usrEngaged, self.mode, self.timeout, self.usrTermination, self.bye, self.usrEngChat, self.lowConf, self.turnTaking


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
    return 'Chat'


class SpeechEventModule(ALModule):
    """ A simple module able to react to facedetection events """
    def __init__(self, name):
        ALModule.__init__(self, name)
        self.tts = ALProxy("ALTextToSpeech")


    def onSpeechDetected(self, *_args):
        print "inside event call"
        print "USER: ", ALMemory.getData("Dialog/LastInput"), '\n'
        print "tskFilled", ALMemory.getData("tskFilled")
        print "ctxTask", ALMemory.getData("ctxTask")
        #print ALDialog.getASRConfidenceThreshold()
        # unsubscribe to stop listening. Will resubscribe during user's turn (TODO: probably will change...)
        global memory
        memory.unsubscribeToEvent("Dialog/LastInput", "SpeechEvent")
        #ALDialog.unsubscribe('my_dialog_example')
        flipTurn()
    
        observeState()
#        print "After OS"
#        decodeAction(nextAction)
#        print "After DA"
#        '''if ALMemory.getData("ctxTask") == "directions":
#            self.tts.say("You are asking for a " + shopList.getShop(ALMemory.getData("shopName")).getCategory() + ". " + 
#            ALMemory.getData("shopName") + "is this way")'''

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
        
        ALTracker.registerTarget("Face", 20)
        ALTracker.track("Face")
        
        # Unsubscribe to the event when talking,
        # to avoid repetitions
        global memory
        memory.unsubscribeToEvent("FaceDetected",
            "HumanGreeter")
        
        #instantiateMemory()
        #readMemoryState(ALMemory)
        observeState()
        #decodeAction(nextAction)
        
        #memory.subscribeToEvent("FaceDetected", "HumanGreeter", "onFaceDetected")

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
    
    tskC = str2bool(ALMemory.getData("tskCompleted"))
    tskF = str2bool(ALMemory.getData("tskFilled"))
    prevA = ALMemory.getData("prevAct") # int
    dist = ALMemory.getData("distance") # int
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
    ALMemory.insertData("distance", 1)
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
    chatbot = ChatBot('Ron Obvious', trainer='chatterbot.trainers.ChatterBotCorpusTrainer')
    chatbot.train("chatterbot.corpus.english")
    
    print chatbot.get_response("")
    
# Initialize state
def resetAttributes():
    ALMemory.insertData("ctxTask","")
    ALMemory.insertData("shopName","")
    ALMemory.insertData("tskFilled","False")
    

def observeState():
    global some_state
    some_state = generateState()
    some_state.printState()
    
    global nextAction
    nextAction = action(some_state)
    print "Action selected: ", nextAction, '\n'
    
    decodeAction(nextAction)
      
    
def decodeAction(nextAction):
    if nextAction == "Greet":
        greet()
    #elif nextAction == "Chat":
        #chat(lastUsrInput)
    elif nextAction == "wait":
        wait()
    elif nextAction == "taskConsume":
        taskConsume()
        
    # Write action taken to state
    if actionID[nextAction] != 6:
        ALMemory.insertData("prevAct", actionID[nextAction])
    
    flipTurn()
    observeState()
        
      
def flipTurn():
    if nextAction != None:
        global turn
        turn = not turn
        ALMemory.insertData("turntaking",  bool2str(turn))

def chat(sentence):
    tts = ALProxy("ALTextToSpeech")
    try:
        tts.say(chatbot.get_response(sentence))
    except RuntimeError:
        tts.say("What is your name")
        #pass

def greet():
    tts = ALProxy("ALTextToSpeech")
    tts.say("Hello")
       
def taskConsume(): #giveDirections()
    print "shopName: ", ALMemory.getData("shopName")
    tts = ALProxy("ALTextToSpeech")
    tts.say("You are asking for a " + shopList.getShop(ALMemory.getData("shopName")).getCategory() + "shop. " + 
            ALMemory.getData("shopName") + " is this way!")
    ALMemory.insertData("ctxTask","")
    ALMemory.insertData("tskFilled","False")

def wait():
    memory.subscribeToEvent("Dialog/LastInput", "SpeechEvent", "onSpeechDetected")
    #ALDialog.subscribe('my_dialog_example')
    while True:
        pass

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
    
'''tskC, tskF, prevA, dist, ctx, usrEng, mode, timeout, usrTerm, bye, usrEngC, lowConf, turn'''
some_state = state(False, False, 1, 1, '', True, False, False, False, False, False, False, True)
some_state.printState()
print action(some_state)

# Populate the shop list from file
shopList= ShopList()
#print shopList.getShop("Public").getCategory()


parser = argparse.ArgumentParser()
parser.add_argument("--ip", type=str, default="192.168.1.37",
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

ALDialog.setLanguage("English")



topic_content = ('topic: ~example_topic_content()\n'
                       'language: enu\n'
                       'concept:(food) [fruits chicken beef eggs]\n'
                       'concept:(coffee) [coffee cappucino latte esspresso americano]\n'
                       'concept:(shop) [starbucks costa public "harware electronics"]\n'
                       'u: (I [want "would like"] {some} _~food) Sure! You must really like $1.\n'
                       'u: (how are you today) Hello human, I am fine thank you and you?\n'
                       'u: (Good morning Nao did you sleep well) No damn! You forgot to switch me off!\n'
                       #'u: ([e:FrontTactilTouched e:MiddleTactilTouched e:RearTactilTouched]) $tskFilled = True $ctxTask = directions $shopName = costa\n'
                       'u: (Open the Pod bay doors) I am sorry Dave, I am afraid I can not do that.\n'
                       'u: (* ~coffee) $tskFilled=True $ctxTask=coffee $shopName=costa\n'
                       'u: (e:Dialog/NotUnderstood) Sorry \n'
                       #'u: (e:Dialog/LastInput) $Dialog/LastInput=$Dialog/LastInput \n'
                       'u: (* _~shop) $tskFilled=True $ctxTask=directions $shopName=$1\n') 


# Loading the topics directly as text strings
topic_name = ALDialog.loadTopicContent(topic_content)

# Activating the loaded topics
ALDialog.activateTopic(topic_name)

ALDialog.subscribe('my_dialog_example')

#print "shopName", ALMemory.getData("shopName"), '\n'

# Reset some state variables in ALMemory
#resetAttributes()

global SpeechEvent
SpeechEvent = SpeechEventModule("SpeechEvent")

global HumanGreeter
HumanGreeter = HumanGreeterModule("HumanGreeter")

# Subscribe to the speech event:
memory = ALProxy("ALMemory")
memory.subscribeToEvent("Dialog/LastInput", "SpeechEvent", "onSpeechDetected")
memory.subscribeToEvent("FaceDetected", "HumanGreeter", "onFaceDetected")

instantiateMemory()


try:
    while True:
        time.sleep(1)
except KeyboardInterrupt:
    print
    print "Interrupted by user, shutting down"
    ALTracker.stopTracker()
    ALTracker.unregisterAllTargets()
    myBroker.shutdown()
    sys.exit(0)

