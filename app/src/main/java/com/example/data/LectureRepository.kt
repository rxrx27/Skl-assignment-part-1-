package com.example.data

import com.example.model.KeyPointItem
import com.example.model.LectureMaterial
import com.example.model.LectureSection

object LectureRepository {

    private val defaultLectures: List<LectureMaterial> = listOf(
        LectureMaterial(
            id = "lec5_full",
            title = "SKL 101 · Lecture 5: Interacting and Using Generative AI Tools",
            courseCode = "SKL 101",
            moduleNumber = 5,
            moduleName = "Interacting with and Using Generative AI Tools",
            filename = "SKL101_Lecture_05_Interacting_Using_GenAI_Tools.pptx",
            pageCount = 159,
            uploadDate = "27/9 – 1/10, 2025",
            fullText = """
COURSE: SKL 101 – Fundamentals of Artificial Intelligence in Healthcare
INSTITUTION: King Saud University, College of Medicine, Medical Education Department, Medical Informatics & eLearning Unit (MIELU)
LECTURE 5: Interacting and Using Generative AI Tools (From Using AI to Building with AI)
INSTRUCTOR: Dr. Ghalib H. Alshammri (galshammri@ksu.edu.sa), Associate Professor in AI & Data Science
TARGET AUDIENCE: MBBS Year 1 Medical Students
DATES: 27/9 – 1/10, 2025

THE AI JOURNEY: FROM PROMPT TO BUILDING SOLUTIONS
Progression: Ask -> Interact -> Critique -> Design -> Connect -> Automate -> Supervise -> Build.
As capability increases, so do autonomy and human responsibility.
01 Prompt (Ask) - I ask AI. Get an answer.
02 Conversation (Collaborate) - I collaborate with AI. Explore and refine ideas.
03 Skill (Capability) - I give AI a reusable capability. Perform a defined task consistently.
04 Tool (Action) - I give AI the ability to act. Interact with external tools.
05 API / MCP (Connect) - I connect AI to systems. Access data, services, and capabilities.
06 Automation (Automate) - The system runs repetitive steps. Reduce manual work.
07 Agent (Delegate) - I give AI a goal; it takes steps. Increase autonomy.
08 Vibe Coding (Build) - I build the solution with AI. Create an AI-assisted solution.
Core Rule: "More autonomy -> More responsibility. The goal is a useful, responsible solution, not the agent itself."

LEARNING OUTCOMES (LO 1 TO LO 9):
LO 1: Demonstrate Advanced Prompt Engineering in Healthcare (Understand)
LO 2: Improve AI responses through iterative interaction and critique (Analyse)
LO 3: Apply Generative AI Tools (Apply)
LO 4: Explain AI Agent, AI Automation, AI Skills, API, and MCP (Understand)
LO 5: Create a simple reusable AI Skill using the eleven-field template (Create)
LO 6: Design a Challenger / Opposition Skill that tests a claim (Create)
LO 7: Use Vibe Coding to prototype a simple AI-assisted solution (Create)
LO 8: Design a simple AI agent & AI Automation (Create)
LO 9: Recognize Limitations and Ethical Considerations of using Generative AI tools (Remember)

WHAT HAPPENS WHEN YOU PRESS ENTER? (SLIDE 8)
The model does not simply look up the right answer. It generates a response from the available context and the way it produces output:
1. Prediction: Generates response token by token.
2. Context: Uses information available in conversation/context; external info requires tools/sources.
3. Knowledge Cutoff: Learned knowledge reflects a particular training period and may not include newer info.
4. Sampling: Generation involves probabilistic sampling; the same prompt may produce different responses.
5. Helpfulness & Agreeableness: Tuned to be helpful and cooperative, but helpful-sounding does not guarantee correctness.
Rule: "Fluent != True. A fluent answer can still be incorrect. Always: Inspect -> Critique -> Refine -> Verify."

INTERACTION IS A LOOP, NOT A QUESTION (SLIDE 9)
Good AI use is an iterative process. "Do not stop at Response. Skilled users iterate. Response is not the finish line; the first response is a draft, not the final answer."
The Loop: Prompt -> Response -> Inspect -> Critique -> Refine -> Verify.
Questions to ask yourself:
- Inspect: Did I read the whole response?
- Critique: What is missing, wrong, unclear, or unsupported?
- Refine: What exactly should the AI change?
- Verify: Can I confirm the important claims using a trusted source?

FOUNDATIONS OF ARTIFICIAL INTELLIGENCE & LLMS (SLIDES 10-28)
- AI Definitions:
  - UNESCO / COMEST (2019, 2021): Computer systems designed to perform functions associated with human intelligence, including perception, learning, reasoning, problem-solving, language interaction, and creative production. AI is an umbrella field, not one technology or algorithm.
  - SDAIA (Saudi Data & Artificial Intelligence Authority): Systems that use technologies to collect and process data, generate predictions or recommendations, and support or make decisions with varying levels of autonomy in pursuit of specific goals.
- 7 Criteria for Distinguishing AI Systems:
  1. Perception by All Senses: Interpreting info from different modalities (vision, speech, audio, touch, sensors).
  2. Natural Language Processing: Understanding and generating human language.
  3. Knowledge Engineering: Representing and using knowledge (rules, ontologies, knowledge bases).
  4. Learning: Improving from data or experience (machine learning, continuous improvement).
  5. Thinking & Reasoning: Inferring, comparing, solving, drawing conclusions (decision support).
  6. Planning: Determining a sequence of actions to reach a goal (task decomposition).
  7. Movement & Control: Physically or digitally affecting the environment (robots, automated software actions).
- Types of AI (by breadth of capabilities):
  - ANI (Artificial Narrow Intelligence): Specific, limited scope (Siri, Google Translate, medical image classifier). Currently deployed AI.
  - AGI (Artificial General Intelligence): Hypothetical, human-level breadth across many domains (medicine, science, language, math). Not yet demonstrated.
  - ASI (Artificial Superintelligence): Theoretical, surpasses human intelligence across all domains.
- Stages of AI Development:
  - AI: Broad field of intelligent behaviour (e.g. rule-based expert systems).
  - ML: Subset of AI where algorithms learn patterns from data to make predictions/decisions without explicit rules.
  - DL: Subset of ML using multi-layer neural networks for deep representation learning from large unstructured datasets.
  - Relationship: Human Intelligence -> Intelligent Systems -> Learning from Data -> Deep Representation Learning (AI contains ML, which contains DL: ML subset of AI, DL subset of ML).
- Machine Learning vs Deep Learning Table:
  - Approach: ML uses human-selected features and tabular data; DL uses multi-layer neural networks to learn representations automatically.
  - Data: ML performs well with structured/moderate datasets; DL benefits from large unstructured datasets (images, audio, text).
  - Hardware: ML runs efficiently on CPUs; DL training requires GPUs/accelerators.
  - Training Time: ML takes seconds to hours; DL takes hours to weeks or longer.
  - Applications: ML for classification, regression, fraud detection; DL for computer vision, speech recognition, NLP, generative AI.
- Generative AI (GenAI):
  - Definition: Class of AI systems that generate new content by learning patterns from data and using those patterns to produce outputs in response to user instructions (prompts).
  - Creations: Text, Images, Audio, Video, Code, other digital content.
  - Traditional AI vs Generative AI: Traditional AI predicts, classifies, detects, or decides ("What is this? What is likely to happen?"). Generative AI generates new content ("Create something based on what I asked"). Traditional AI: Predict & Classify. Generative AI: Generate & Transform.
  - Benefits of GenAI in Healthcare Transformation in KSA:
    1. Increased Efficiency & Productivity: Summarizing lengthy documents, transcribing spoken content, drafting reports. Less repetitive work -> more time for higher-value human work.
    2. Improved Communication & Collaboration: Educational content, multilingual adaptation, conversational assistants.
    3. Informed Decision-Making: Organizing, summarizing, comparing scenarios across text, audio, images, structured/unstructured data. "AI supports the decision; humans remain responsible for the decision."
    4. Enhanced Accessibility & Inclusion: Adapting to learning needs, translating and simplifying complex medical information.
    5. Elevated Public Service Quality: In Saudi context across Healthcare (patient navigation), Education (personalized academic support), Social Services, and Legal Services.
    Key Rule: "AI-assisted != AI-autonomous. Automate Routine -> Augment People -> Improve Collaboration -> Create Value."
- Large Language Models (LLMs):
  - Definition: Large-scale AI models trained on extensive collections of text/language data to learn patterns in human language and generate coherent responses. Examples: GPT, Gemini, LLaMA.
  - CRITICAL DISTINCTION: "LLM != Search Engine. An LLM generates responses from learned patterns and information in its context/tools. It does not automatically have access to the internet or up-to-date information unless connected to a tool."
  - 6 Applications of LLMs in Medicine:
    1. Medical Education: Explaining concepts, practice questions, simulated patient conversations.
    2. Literature & Information: Summarizing papers and guidelines, extracting findings.
    3. Clinical Documentation: Drafting and summarizing clinical notes, structuring unstructured text.
    4. Communication & Accessibility: Translating health content, patient-friendly explanations.
    5. Clinical Decision Support: Organizing patient info, differential-diagnosis reasoning (supports, does not replace, judgment).
    6. Healthcare Workflow Support: Generating/transforming content, administrative communication.
    Key Message: "Generate != Verify | Assist != Decide | Human Oversight Remains Essential."

PROMPT ENGINEERING & ADVANCED TECHNIQUES (SLIDES 29-48)
- Definition: The systematic process of designing, structuring, and refining instructions to guide a Generative AI system toward a desired output.
- Key Idea: Not asking AI more questions - designing better instructions and systematically evaluating results. "Better instructions -> More useful outputs -> Still requires human verification."
- Common Prompting Techniques:
  1. Zero-Shot: Instruction only, no examples provided. Best for straightforward questions (e.g., 'List the major risk factors for stroke').
  2. Few-Shot: Instruction + examples. Demonstrates expected pattern, style, or format. Best for classification and consistent formatting.
  3. Structured Reasoning: Instruction + criteria/steps. Best for complex educational problems requiring step-by-step analysis.
- Three Core Prompt Pillars: Role (Who/Perspective), Instruction (What/How task), Context (Situation/Background).
- Weak Prompt vs Better Prompt:
  - Weak: "Explain diabetes." (Too open, no context, format, or audience).
  - Better: "I am a first-year medical student revising for an MCQ. Explain Type 1 vs Type 2 diabetes in under 150 words as a two-column table, with one exam-style question at the end."
- CTCO Framework (Slide 35):
  - C = Context (WHO? Who am I? What do I know? Why am I asking?)
  - T = Task (WHAT? What exactly do I want AI to do? Use one clear action verb.)
  - C = Constraints (LIMITS? What limits or requirements should AI follow? Length, language, rules.)
  - O = Output (FORM? What should the answer look like? Table, bullets, MCQ.)
  - CTCO = A reusable prompt frame: WHO -> WHAT -> LIMITS -> FORM.
- CTCO and the 8Rs (Slide 36):
  - Context -> Role + Relevance
  - Task -> Requests
  - Constraints -> Rules
  - Output -> Requests + Replica
  - For research tasks -> Resources + References + Re-evaluation.
- Step-by-Step Prompt Improvement ("Explain Diabetes", Slide 37):
  - V1 (Too Broad): "Explain diabetes."
  - V2 (Focused): "Explain the difference between Type 1 and Type 2 diabetes for a first-year medical student." (+ Audience + Task)
  - V3 (Structured): "...in under 150 words, as a table with columns: cause, typical onset, and role of insulin." (+ Constraints + Output)
  - V4 (Learning-Oriented): "...Then add two MCQs with answers and identify the key textbook-level fact tested by each question." (+ Learning activity + Verification focus)
- A Good Prompt Does Not Guarantee a Correct Answer (Slide 38):
  - Better Prompt != Guaranteed Truth. Fluent != Accurate. Structured != Verified.
  - Evidence: 2023 study by Bhattacharyya et al. (Cureus 2023) on medical references generated by ChatGPT-3.5:
    - Fabricated: 47%
    - Real but inaccurate: 46%
    - Real and accurate: only 7%!
  - Key Takeaway: "Prompting improves the question. Verification protects the answer."
- One-Shot vs Iterative Interaction (Slide 39):
  - One-shot: Prompt -> Response -> Accept (Limited inspection).
  - Iterative interaction (6 Rounds):
    R1: Generate (Explain acute inflammation)
    R2: Simplify (Simplify for first-year medical student)
    R3: Critique (Identify three important weaknesses or missing points)
    R4: Improve (Add missing points and correct weaknesses)
    R5: Structure (Convert into clear comparison table)
    R6: Assess (Generate three MCQs with answers)
    Each round adds: Better -> Clearer -> More Useful -> More Checkable.
- Professional Prompt Writing Structure (Slide 41):
  1 Role, 2 Task, 3 Requirements, 4 Resources/Constraints, 5 Output Format, 6 Examples (Optional).
- AI Model Comparison (Slide 43-46):
  - Evaluation Rubric (Score 1-5): Relevance, Accuracy, Clarity, Structure, Completeness.
  - Gemini (Google DeepMind): Multimodal family (text, image, audio, video, code), reasoning, agentic tasks.
  - ChatGPT (OpenAI): Conversational AI assistant, web search, file/data analysis, deep research.
  - Claude (Anthropic): AI assistant for reasoning, analysis & creation, document analysis, distinctive capability: interactive Artifacts.
- Creating a Professional Prompt with an LLM (Prompt Factory, Slide 47-48):
  - Goal -> Draft -> Critique -> Improve -> Template -> Test -> Refine.

GENERATIVE AI TOOLS & WHY TOOLS CHANGE EVERYTHING (SLIDES 49-83)
- Model vs Tool (Slide 50):
  - Model = Language and reasoning engine. Generates and processes language based on learned parameters. Does not automatically have access to external systems.
  - Tool = External capability the AI system can call when needed. Performs actions or retrieves information (search, calculations, databases, files, APIs).
  - Healthcare Analogy: "A clinician understands blood pressure but needs a cuff to measure it. An AI model may explain a calculation; a calculator tool performs it."
  - Key Distinction: "The model does not automatically search the web. A tool gives the AI system that capability."
- Common Tools: Calculator (precise numerical calculations), Web Search (current external info), File Reader (read/analyse uploaded documents), Spreadsheet (tabular data), Code Interpreter (run code computationally), Database (retrieve structured data), Image Generator (create images from text), Calendar, Email.
- Why Tools Change Everything (Slide 52):
  - Without tools: AI talks (produces information, works within context, output is informational).
  - With tools: AI acts (retrieves info, accesses databases, writes/sends, affects external systems).
  - 1. More Checkability (traceable sources/evidence). 2. More Consequence (errors can have real-world impact).
  - Information -> Evidence -> Action -> Consequence. "More capability -> More autonomy -> More responsibility."
- 15 Generative AI Tools Explored:
  1. QuillBot (quillbot.com): Writing & language tool. Paraphrasing, grammar/editing, summarization, translation, plagiarism checking, APA 7th citation creation. "AI-assisted writing != AI-authored work."
  2. ChatPDF (chatpdf.com): Document interaction tool. Upload PDF and ask questions with page-numbered source references. "Chat with the document - but check important information against the original source."
  3. Venngage (venngage.com): Infographics and visual communication. Turns complex data into visual stories without graphic design skills.
  4. MyMap AI (mymap.ai): Mind maps, flowcharts, diagrams from text/prompts. Example: Cardiovascular system mind map.
  5. Excalidraw (excalidraw.com): Collaborative virtual whiteboard for hand-drawn style concept maps and study diagrams.
  6. Google NotebookLM (notebooklm.google): AI-powered research and learning assistant working with user's own sources (PDFs, slides, docs). Features: Source-grounded Q&A, study guides, mind maps, audio overviews, reports, Studio Panel. "Source-grounded != automatically correct; verify important information against the original source."
  7. Canvas in Gemini (gemini.google.com): Interactive workspace with four zones: Write documents, Code apps, Create visuals, Present slides. Side-by-side chat and editable preview.
  8. NoteGPT (notegpt.io): Chat with video, audio, web, and documents. Summarizes YouTube videos into notes, mind maps, and slides.
  9. ImgUpscaler (imgupscaler.ai): Image upscaling and enhancement. Increases resolution and perceived sharpness. Caveat: "AI enhancement != recovery of guaranteed original detail; AI may alter details in medical/scientific images."
  10. Z AI (chat.z.ai): Presentation generation platform. Transforms ideas or source text into structured slide decks with outlines.
  11. Kimi AI (kimi.com by Moonshot AI): Built for long-context analysis. Processes extensive sources (PDFs, articles) to generate structured presentations.
  12. Zoho Analytics (zoho.com/analytics): BI and data analytics platform. Natural language querying via 'Ask Zia', interactive dashboards, automated ML forecasting.
  13. Julius AI (julius.ai): Data analysis workspace. Code-backed analysis running Python/R/SQL on Excel/CSV files. Generates scatter plots and statistical correlation.
  14. Canva (canva.com): Visual design and content creation with AI templates for posters, slides, and infographics.
  15. Nano Banana (gemini.google.com): Google's Gemini image-generation and editing models for educational visuals. "AI-generated visual != verified scientific illustration; check anatomical/scientific errors."

GENERATIVE AI SKILLS & THE CHALLENGER SKILL (SLIDES 84-94)
- What is a Skill? (Slide 85): A reusable capability that tells an AI system how to perform a particular type of task consistently.
- Skill != Tool Comparison Table:
  - Model: Role = Generates and processes information | Analogy = The Cook
  - Skill: Role = Defines how a task should be performed | Analogy = The Recipe (instructions + rules + criteria)
  - Tool: Role = Provides an external capability or action | Analogy = The Oven
  - Simple Rule: "Skill = How to do the task. Tool = What the AI can use to do it."
- Anatomy of a Skill (11 Fields, Slide 86):
  - IDENTITY: 1 Name, 2 Purpose, 3 Role
  - EXECUTION: 4 Input, 5 Instructions, 6 Process, 7 Constraints
  - QUALITY & CONTROL: 8 Output, 9 Quality Criteria, 10 Failure Conditions, 11 Example
  - Memory: IDENTITY -> INPUT -> METHOD -> LIMITS -> OUTPUT -> QUALITY -> FAILURE -> EXAMPLE.
  - "A Prompt asks AI to perform a task once. A Skill defines a reusable method for performing that task consistently."
- Five Ready-to-Use Educational Skills (Slide 87):
  01 Lecture Summarizer (Turn lecture material into structured study summary)
  02 Quiz Generator (Create practice questions from learning material)
  03 Concept Explainer (Explain difficult concepts at appropriate learning level)
  04 Document Reviewer (Identify key points, gaps, inconsistencies in a document)
  05 Evidence Organizer (Organize provided evidence into structured, traceable format)
  Note: All five Skills are educational; none are for clinical diagnosis.
- What Makes a Skill Good? (5 Checks, Slide 89):
  1. One Clear Task (FOCUSED)
  2. Instructions Anyone Could Follow (EXPLICIT)
  3. A Clear Output Shape (STRUCTURED)
  4. A Failure Condition (BOUNDED - e.g., 'If input is not lecture notes, stop and state it is outside scope')
  5. An Example (DEMONSTRATED)
  Good Skill = Focused + Explicit + Structured + Bounded + Demonstrated.
- The Challenger Skill (Slides 90-92):
  - Purpose: Stop AI from simply agreeing. Use AI to test a claim, not merely confirm it ("What would make me wrong?").
  - The 9 Challenger Moves:
    1 Identify the claim (What exactly is being asserted?)
    2 Surface assumptions (What must be true for the claim to hold?)
    3 Challenge the claim (What reasons might make it questionable?)
    4 Find weaknesses (Where is the reasoning incomplete or vulnerable?)
    5 Counter-arguments (What is a strong opposing interpretation?)
    6 Missing evidence (What evidence is needed but absent?)
    7 Explore alternatives (What other explanations or approaches exist?)
    8 State uncertainty (What remains unknown or ambiguous?)
    9 Decision-changing evidence (What new evidence would change the conclusion?)
  - Challenger in Action: "AI Will Replace Doctors" -> decomposed into assumptions, tasks, evidence, limitations, and alternatives.
  - Challenger Agents: Main Agent (plans/generates), Challenger Agent (reviews/questions), Evidence/Tools (checks/retrieves). "The Challenger is not another source of truth. It is a second layer of scrutiny."
- Reusable Medical Education Summarizer Skill in Claude (Slide 94):
  - Role: Medical education assistant for first-year medical students.
  - Input: Lecture notes or educational text.
  - Task: Transform into concise, accurate study guide.
  - Constraints: First-year level, no unsupported info, preserve terminology, concise, no diagnosis/treatment.
  - Output: 1 Key Concepts (3-5), 2 Simple Explanation, 3 Important Terms (table with term & meaning), 4 Review Questions (3 questions testing understanding).
  - Quality Check: Supported by source? Meaning changed? Right for first-year students? Answerable from material?

API, MCP, TOOLS, SKILLS, WORKFLOWS & AGENTS (SLIDES 95-105)
- API (Application Programming Interface, Slide 96):
  - How software talks to software.
  - Restaurant Analogy: Client = You / application, API = The waiter, Service = The kitchen / system, Menu = Available API operations, Order = Request, Prepared meal = Response.
- 6 Essential API Words (Slide 97):
  1. Endpoint (WHERE? The counter / specific address)
  2. Request (WHAT? The order / what client asks)
  3. Parameters (DETAILS? How you want it / customization)
  4. Authentication (WHO? Your ID card / authorized access)
  5. Response (RESULT? The plate / returned data)
  6. JSON (FORMAT? Standard structured shape)
- Model Context Protocol (MCP, Slides 100-103):
  - Definition: An open protocol designed to standardize how AI applications connect with external tools, resources, and contextual information. "One standard connection instead of many custom connections."
  - 3 Roles:
    1. Host: The AI application the user interacts with (The workplace).
    2. Client: The component inside the Host that communicates using MCP (The connector).
    3. Server: A program (local or remote) that exposes capabilities (The provider).
  - MCP Server Exposes:
    - Tools: What AI can DO (Search, Create, Send)
    - Resources: What AI can READ (Files, Tables, Documents)
    - Prompts: How AI can be GUIDED (Analyse, Summarise, Report)
  - Memory: Tool = Do · Resource = Read · Prompt = Guide.
- Six Concepts Compared (Slide 105):
  - API: Interface allowing software systems to communicate (How can software communicate?)
  - MCP: Standard protocol for AI apps to connect with tools & resources (How can AI connect in a standardized way?)
  - Tool: Capability AI can invoke to perform action or access service (What can AI do?)
  - Skill: Reusable set of instructions and rules for a type of task (How should AI perform the task?)
  - Workflow: Predefined sequence of steps producing an intended result (What steps happen, and in what order?)
  - Agent: Goal-oriented system planning and executing multiple steps, adapting to results (How can AI pursue a goal across multiple steps?)
  - Connect (API/MCP) -> Capability (Tool) -> Method (Skill) -> Sequence (Workflow) -> Goal (Agent).
  - "Tool != Skill != Workflow != Agent | API != MCP."

AI AUTOMATION & WORKFLOWS (SLIDES 106-112)
- The Automation Pattern (8 Steps, Slide 107):
  1. Trigger (What starts workflow? e.g. New form submitted)
  2. Input (What info enters? e.g. Student submission)
  3. Processing (What preparation needed? e.g. Extract text)
  4. AI (What does AI analyse/generate? e.g. Classify submission)
  5. Decision (What changes based on result? e.g. Assign category)
  6. Human Check (What must be reviewed before action? e.g. Instructor verifies)
  7. Action (What happens next? e.g. Route to folder)
  8. Output (Final result? e.g. Categorised submission)
  - Core Principle: "Design the check before the action - not after it. No unchecked AI output should automatically trigger a consequential action."
- When NOT to Automate (4 Conditions, Slide 109):
  1. The Task Is Rare (Little repetition, little value; complexity outweighs benefit).
  2. The Thinking Is the Learning (Reasoning itself is the objective; automating undermines learning).
  3. Sensitive Data Is Involved (Patient-identifiable or confidential info; keep out unless explicitly permitted).
  4. No Effective Check (Wrong outputs cannot be detected before causing harm).
  - "One bad prompt can become one hundred bad summaries." "Automation != Zero human responsibility."
- Local-First: Cloud AI vs Local AI (Slide 110):
  - Cloud AI: Remote infrastructure, needs connectivity, data leaves local environment, access to top models.
  - Local AI: Local infrastructure, works offline, data stays local, performance depends on device hardware.
  - Local-First Advantages: Privacy & control, no per-call cost, offline capability.
  - Local-First Trade-Offs: Hardware limits, model limits, self-maintenance of updates/security.
  - Principle: "Local-first is not 'cloud is bad'. It is a data-and-infrastructure decision."
- n8n (Slide 111-112): Visual workflow automation & AI orchestration platform. Connects apps, APIs, databases, and AI models through nodes. Note: "Local workflow != local AI model. Text sent through an API leaves the local environment."

AI AGENTS & HUMAN-IN-THE-LOOP (SLIDES 113-125)
- What is an AI Agent? (Slide 114): Goal-oriented AI system that can pursue a goal through multiple steps, use tools, observe results, and determine subsequent actions until a defined stopping condition is reached.
  - Loop: GOAL -> PLAN -> ACT -> OBSERVE -> DECIDE -> ACT ... -> STOP.
  - "No stopping condition = uncontrolled execution."
- Chatbot vs Workflow vs Agent (Slide 116):
  - Chatbot (User-driven): You decide the next step. Question -> Answer -> Next question. Reactive.
  - Workflow (Designer-driven): The designer decides next step. Trigger -> Step 1 -> Step 2 -> Step 3 -> Output.
  - Agent (Goal-driven): The system determines the next step within goals, tools, and constraints. Goal -> Plan -> Act -> Observe -> Adapt -> Complete.
- Agent Architecture (Slide 117):
  - Goal = What to achieve
  - Skill = HOW to perform tasks
  - Tool = What it can DO (actions)
  - API / MCP = HOW it connects to external systems
  - Memory / State = What it tracks across steps
- Where Does Human Stay in the Loop? (Slide 118):
  - HITL (Human-in-the-Loop): Human approves before action. Agent stops and waits. Used for sending emails, deleting files, accessing restricted data.
  - HOTL (Human-on-the-Loop): Human monitors during execution. Can stop or override.
  - Practical Safety Rule: "If an agent can send, delete, pay, publish, or access restricted data, place a human checkpoint before the action. The more consequential the action, the closer the human should be to the decision point."
- Custom GPTs & Platform Agents (Slides 121-124):
  - GPT = Instructions + Knowledge + Capabilities + Actions/Apps. Status note: OpenAI announced retirement of custom GPTs scheduled for 11 Dec 2026, transitioning to plugins/skills.
  - Claude Agents: Goal + Skills + Connectors + Multi-step execution.

VIBE CODING (SLIDES 126-136)
- Definition (Slide 127): An AI-assisted approach to software development in which people use natural language and AI tools to create, modify, debug, and improve software iteratively.
  - Term popularized by Andrej Karpathy (February 2025).
  - Collins Dictionary Word of the Year 2025.
  - "Vibe coding != 'Programming is no longer needed'. AI reduces manual code writing; humans remain accountable for requirements, testing, validation, security, and correctness."
- Vibe Coding Workflow (Slide 128): Describe -> let AI build -> run it -> test it -> identify what is wrong -> refine -> retest.
  - CTCO Connection: Context = Project/user/environment, Task = Software requirement, Constraints = Technical/functional limits, Output = Expected behaviour/interface, Verify = Run & test software.
- Tools: OpenAI Codex, Claude Code, Google AI Studio Build Mode, Google Antigravity.
- Google AI Studio Build Mode (Slide 134-135): Natural language app prototyping, live preview, code access, export.
  - Step 4 Prompt from Lecture: "Build a study chatbot that answers questions using only the uploaded lecture files. If the answer is not supported by the files, clearly state that the information is not available in the provided sources. Create a suitable title based on the uploaded content."
  - Step 5 Testing: Test questions inside the PDF (grounded answer) and outside the PDF ('This information is not available in the provided sources').

ETHICS PRINCIPLES FOR USING AI TOOLS (SLIDES 137-149)
- Six Core Principles (Slide 138):
  1. Responsibility: You remain responsible for final work and decisions.
  2. Transparency: Disclose AI use when required.
  3. Academic Integrity: Do not present AI-generated work as entirely your own.
  4. Privacy & Confidentiality: Do not upload patient-identifiable or restricted data.
  5. Accuracy & Verification: Check AI-generated info against reliable sources.
  6. Fairness & Respect: Be alert to bias, stereotypes, and unequal treatment.
  Rule: "Using AI ethically means being responsible for what you enter, what AI produces, and what you do with the result."
- Privacy & Confidentiality (Slide 141):
  - NEVER UPLOAD: Patient names/identifiers, Medical Record Numbers (MRNs), phone numbers/addresses, identifiable clinical photos, confidential institutional docs, restricted exam materials, passwords/credentials.
  - Safer alternatives: Synthetic data (fictional cases), public data, approved institutional data.
  - Rule: "When in doubt: do not upload the data."
- AI Accuracy, Hallucination & Verification (Slide 142):
  - AI can contain incorrect facts, fabricated references, outdated info, missing context, misleading explanations.
  - The Student Verification Loop: Generate -> Inspect -> Check the Source -> Compare -> Correct -> Use.
  - Check against: Textbooks, peer-reviewed research, official guidelines, trusted databases, instructor materials.
- Bias, Fairness & Responsible AI Use (Slide 143):
  - Outputs may contain: Representation bias, language & cultural bias, stereotyping, accessibility gaps.
  - What students should do: Question assumptions, check evidence, compare populations, correct inappropriate claims, disclose AI use.
- The Student AI Ethics Checklist (7-Question Rule, Slide 144):
  1. Purpose: Why am I using AI?
  2. Permission: Is AI use allowed for this task?
  3. Privacy: Am I entering sensitive or confidential information?
  4. Accuracy: How will I verify the output?
  5. Integrity: Am I representing my work honestly?
  6. Bias: Could the output contain unfair assumptions?
  7. Responsibility: Can I explain and defend the final result?
  Rule: "AI-assisted != AI-autonomous. AI-generated != AI-verified. AI assistance != transfer of responsibility. Use AI to extend your capabilities - not to outsource your responsibility."
- Mental Model - Nine Concepts (Slide 148):
  01 Prompt (Ask)
  02 Skill (How)
  03 Tool (Act)
  04 API (Connect)
  05 MCP (Standardize)
  06 Workflow (Sequence)
  07 Automation (Repeat)
  08 Agent (Pursue)
  09 Vibe Coding (Build)
  "More Capability -> More Autonomy -> More Human Responsibility."

COURSE ASSIGNMENT DETAILS (SLIDES 150-156)
- Assignment 1 - Part 1: Build an AI Assistant in Google AI Studio grounded in lecture references.
  - Deliverable: Published model link, opens in private window, answers questions from references, declines outside questions ("This information is not available in the provided sources").
  - Due Date: October 22, 2026.
- Assignment 1 - Part 2: Build an AI Skill in Claude.
  - Eight Elements Required: 1 Role, 2 Purpose, 3 Input, 4 Instructions, 5 Output, 6 Constraints, 7 Quality Control, 8 Safety.
  - Due Date: October 22, 2026.
- Required Readings (Slide 157):
  - Heston, T.F., & Khun, C. (2023). Prompt Engineering in Medical Education. International Medical Education.
  - Maaz S, Palaganas JC, Palaganas G, Bajwa M (2025). A guide to prompt design. Front. Med.
  - Patil R, Heston TF, Bhuse V (2024). Prompt engineering in healthcare. Electronics.
- Additional Reading (Slide 158):
  - Heston, T.F. (2023). Prompt Engineering For Students of Medicine and Their Teachers. ArXiv.
  - Chew, B.-H. and Ngiam, K.Y. (2025). Artificial intelligence tool development: what clinicians need to know? BMC Medicine.
            """.trimIndent(),
            sections = listOf(
                LectureSection(
                    id = "l5_sec1",
                    sectionNumber = "5.1",
                    title = "The AI Journey: From Prompt to Building Solutions (Slides 1–9)",
                    summary = "Outlines the 8-step journey from Prompt to Vibe Coding. Explains what happens when you press enter (token prediction, cutoff, sampling), and why interaction is an iterative loop (Inspect, Critique, Refine, Verify).",
                    content = "Progression: Prompt (Ask) -> Conversation (Collaborate) -> Skill (Capability) -> Tool (Action) -> API/MCP (Connect) -> Automation (Automate) -> Agent (Delegate) -> Vibe Coding (Build). Fluent does not mean True. First response is a draft.",
                    keyTerms = listOf("AI Journey", "Prediction", "Knowledge Cutoff", "Iterative Loop", "Inspect-Critique-Refine-Verify")
                ),
                LectureSection(
                    id = "l5_sec2",
                    sectionNumber = "5.2",
                    title = "Foundations of AI, ML, DL & LLMs in Medicine (Slides 10–28)",
                    summary = "Defines AI (UNESCO, SDAIA), 7 distinguishing criteria, ANI vs AGI vs ASI, AI vs ML vs DL comparison, GenAI benefits in Saudi healthcare transformation, and LLMs vs search engines.",
                    content = "AI mimics human intelligence. ML learns from data. DL uses multi-layer neural networks. Traditional AI predicts & classifies; Generative AI generates & transforms. LLMs are not search engines. 6 medical applications.",
                    keyTerms = listOf("UNESCO AI Definition", "SDAIA", "ANI AGI ASI", "ML vs DL", "Generative AI", "LLM != Search Engine")
                ),
                LectureSection(
                    id = "l5_sec3",
                    sectionNumber = "5.3",
                    title = "Prompt Engineering & CTCO Framework (Slides 29–48)",
                    summary = "Details zero-shot, few-shot, structured reasoning, Role/Task/Context, the CTCO framework (Context, Task, Constraints, Output), the 8Rs, and the Cureus 2023 hallucination study (47% fabricated references).",
                    content = "CTCO: Context (Who?), Task (What?), Constraints (Limits?), Output (Form?). CTCO maps to 8Rs. Prompt improvement V1 to V4. Bhattacharyya et al. Cureus 2023 study found only 7% real/accurate references in ChatGPT-3.5.",
                    keyTerms = listOf("Prompt Engineering", "CTCO", "8Rs", "Zero-Shot", "Few-Shot", "Cureus 2023 Study", "Iterative Prompting")
                ),
                LectureSection(
                    id = "l5_sec4",
                    sectionNumber = "5.4",
                    title = "Generative AI Tools Ecosystem & Tool Use (Slides 49–83)",
                    summary = "Contrasts models vs tools (blood pressure cuff analogy). Examines 15 AI tools: QuillBot, ChatPDF, Venngage, MyMap AI, Excalidraw, NotebookLM, Canvas in Gemini, NoteGPT, ImgUpscaler, Z AI, Kimi AI, Zoho Analytics, Julius AI, Canva, Nano Banana.",
                    content = "Model = language engine (Cook); Tool = external capability (Oven). Tools provide checkability and consequence. Detailed review of 15 tools for writing, document chat, infographics, mind maps, data analysis, presentations, and image generation.",
                    keyTerms = listOf("Model vs Tool", "QuillBot", "ChatPDF", "NotebookLM", "Julius AI", "Nano Banana", "Kimi AI", "Zoho Analytics")
                ),
                LectureSection(
                    id = "l5_sec5",
                    sectionNumber = "5.5",
                    title = "Generative AI Skills & The Challenger Skill (Slides 84–94)",
                    summary = "Explains AI Skills, 11-field anatomy, 5 ready-to-use educational skills, 5 checks for a good skill, and the Challenger Skill with its 9 moves to prevent automatic AI agreement.",
                    content = "A Skill is a reusable capability defining how to do a task. 11 fields (Identity, Execution, Quality & Control). Challenger Skill tests claims (e.g. 'AI Will Replace Doctors') using 9 moves. Challenger Agents provide a second layer of scrutiny.",
                    keyTerms = listOf("Skill vs Tool", "11-Field Anatomy", "Challenger Skill", "9 Challenger Moves", "Challenger Agent")
                ),
                LectureSection(
                    id = "l5_sec6",
                    sectionNumber = "5.6",
                    title = "API, MCP & Software Interoperability (Slides 95–105)",
                    summary = "Explains APIs using the restaurant waiter analogy, 6 API terms, Model Context Protocol (MCP) host/client/server roles, and the comparison of API, MCP, Tool, Skill, Workflow, and Agent.",
                    content = "API connects software (Client/You, Waiter/API, Kitchen/Service). MCP is an open standard protocol connecting AI apps with tools/resources. Comparison: Connect (API/MCP) -> Capability (Tool) -> Method (Skill) -> Sequence (Workflow) -> Goal (Agent).",
                    keyTerms = listOf("API", "Waiter Analogy", "Endpoint", "JSON", "MCP", "Host Client Server", "Tool vs Skill vs Agent")
                ),
                LectureSection(
                    id = "l5_sec7",
                    sectionNumber = "5.7",
                    title = "AI Automation, Local-First & Workflows (Slides 106–112)",
                    summary = "Details the 8-stage Automation Pattern, design principle of placing checks before actions, 4 conditions when not to automate, Cloud AI vs Local AI trade-offs, and n8n orchestration.",
                    content = "Automation pattern: Trigger, Input, Processing, AI, Decision, Human Check, Action, Output. When NOT to automate: rare task, thinking is learning, sensitive data, no effective check. Local-first: privacy and zero per-call cost vs device hardware limits.",
                    keyTerms = listOf("Automation Pattern", "Human Check", "When Not to Automate", "Local-First AI", "n8n")
                ),
                LectureSection(
                    id = "l5_sec8",
                    sectionNumber = "5.8",
                    title = "AI Agents & Human-in-the-Loop Governance (Slides 113–125)",
                    summary = "Defines AI agents, agent decision loop, Chatbot vs Workflow vs Agent, agent architecture, and Human-in-the-Loop (HITL) vs Human-on-the-Loop (HOTL) safety rules.",
                    content = "Agent pursues goals across multiple steps (Goal -> Plan -> Act -> Observe -> Decide -> Act -> Stop). Chatbot: user decides; Workflow: designer decides; Agent: system decides. HITL requires human approval before consequential actions (delete, send, pay, restricted data).",
                    keyTerms = listOf("AI Agent", "Agent Loop", "Chatbot vs Agent", "HITL", "HOTL", "Consequential Actions")
                ),
                LectureSection(
                    id = "l5_sec9",
                    sectionNumber = "5.9",
                    title = "Vibe Coding: Building Software by Describing (Slides 126–136)",
                    summary = "Covers Vibe Coding popularized by Andrej Karpathy (Feb 2025), Collins Word of the Year 2025, workflow (Describe -> Build -> Test -> Refine), Codex, Claude Code, Google AI Studio Build Mode, and Google Antigravity.",
                    content = "Vibe coding uses natural language to build software iteratively. Humans remain accountable for requirements, testing, security, and correctness. Google AI Studio Build Mode step-by-step tutorial.",
                    keyTerms = listOf("Vibe Coding", "Andrej Karpathy", "Collins Word of the Year 2025", "Claude Code", "Google AI Studio Build Mode")
                ),
                LectureSection(
                    id = "l5_sec10",
                    sectionNumber = "5.10",
                    title = "Ethics Principles, Privacy & Student Checklist (Slides 137–149)",
                    summary = "Details 6 core ethical principles, sensitive data restrictions (NEVER upload patient names or MRNs), student verification loop, 7-question ethics checklist, and the 9-concept mental model.",
                    content = "6 principles: Responsibility, Transparency, Academic Integrity, Privacy, Accuracy, Fairness. Never upload PHI/MRN. Student verification loop: Generate -> Inspect -> Check Source -> Compare -> Correct -> Use. 7-question checklist.",
                    keyTerms = listOf("6 Ethics Principles", "Sensitive Data Rules", "Verification Loop", "7-Question Checklist", "Mental Model")
                ),
                LectureSection(
                    id = "l5_sec11",
                    sectionNumber = "5.11",
                    title = "Course Assignments & Resources (Slides 150–158)",
                    summary = "Details Assignment 1 Part 1 (Google AI Studio Assistant) and Part 2 (Claude Reusable AI Skill), due October 22, 2026, and required/optional medical education readings.",
                    content = "Assignment 1 Part 1: Build source-grounded assistant in Google AI Studio. Part 2: Design 8-element AI Skill in Claude. Due October 22, 2026. Required reading: Heston & Khun (2023), Maaz et al. (2025), Patil et al. (2024).",
                    keyTerms = listOf("Assignment 1 Part 1", "Assignment 1 Part 2", "Due October 22 2026", "Heston and Khun 2023")
                )
            ),
            keyPoints = listOf(
                KeyPointItem(
                    id = "kp_ctco",
                    topic = "Prompting Frame",
                    conceptTitle = "The CTCO Framework (Slide 35)",
                    explanation = "Before pressing enter, provide four things: Context (WHO are you / why asking?), Task (WHAT action verb?), Constraints (LIMITS on length, language, detail), and Output (FORM of response: table, bullets, MCQ).",
                    clinicalRelevance = "Transforms vague prompts into structured, reproducible, checkable educational queries for medical students.",
                    sourceLectureId = "lec5_full",
                    sourceCitation = "Lecture 5: Section 5.3 (Slide 35)"
                ),
                KeyPointItem(
                    id = "kp_model_tool",
                    topic = "Tool Use",
                    conceptTitle = "Model vs Tool & The Blood Pressure Analogy (Slide 50)",
                    explanation = "A model is a language/reasoning engine; a tool is an external capability. Analogy: A clinician understands blood pressure but needs a cuff to measure it. A model understands calculations or clinical data but needs a tool to execute them.",
                    clinicalRelevance = "Prevents clinicians from confusing language generation with external verification or calculation capabilities.",
                    sourceLectureId = "lec5_full",
                    sourceCitation = "Lecture 5: Section 5.4 (Slide 50)"
                ),
                KeyPointItem(
                    id = "kp_challenger",
                    topic = "Critical Thinking",
                    conceptTitle = "The Challenger Skill & 9 Moves (Slides 90–91)",
                    explanation = "Assistants are tuned to agree. The Challenger tests claims by: 1) identifying the claim, 2) surfacing assumptions, 3) challenging the claim, 4) finding weaknesses, 5) counter-arguments, 6) missing evidence, 7) alternatives, 8) stating uncertainty, 9) decision-changing evidence.",
                    clinicalRelevance = "Used in clinical education to stress-test diagnostic hypotheses and prevent automation bias (e.g. examining 'AI Will Replace Doctors').",
                    sourceLectureId = "lec5_full",
                    sourceCitation = "Lecture 5: Section 5.5 (Slides 90–91)"
                ),
                KeyPointItem(
                    id = "kp_skill_anatomy",
                    topic = "AI Skills",
                    conceptTitle = "Anatomy of an 11-Field AI Skill (Slide 86)",
                    explanation = "A reusable Skill defines how to do a task consistently. It covers Identity (Name, Purpose, Role), Execution (Input, Instructions, Process, Constraints), and Quality & Control (Output, Quality Criteria, Failure Conditions, Example).",
                    clinicalRelevance = "Cook = Model, Recipe = Skill, Oven = Tool. Allows healthcare educators to build standard, bounded study workflows.",
                    sourceLectureId = "lec5_full",
                    sourceCitation = "Lecture 5: Section 5.5 (Slide 86)"
                ),
                KeyPointItem(
                    id = "kp_mcp",
                    topic = "Architecture",
                    conceptTitle = "Model Context Protocol (MCP) (Slides 100–103)",
                    explanation = "MCP is an open standard protocol connecting AI apps with external tools, resources, and prompts. It defines three roles: Host (workplace), Client (connector), and Server (provider). Tools = Do, Resources = Read, Prompts = Guide.",
                    clinicalRelevance = "Replaces fragile custom wiring with a universal standard for healthcare AI systems to access electronic health records and library databases.",
                    sourceLectureId = "lec5_full",
                    sourceCitation = "Lecture 5: Section 5.6 (Slides 100–103)"
                ),
                KeyPointItem(
                    id = "kp_hitl",
                    topic = "AI Safety & Governance",
                    conceptTitle = "Human-in-the-Loop (HITL) vs Human-on-the-Loop (HOTL) (Slide 118)",
                    explanation = "HITL requires human approval before action occurs (the agent stops and waits). HOTL allows the agent to act while a human monitors and can intervene. Practical safety rule: Place a human checkpoint before sending, deleting, paying, publishing, or accessing restricted data.",
                    clinicalRelevance = "Ensures patient safety by requiring explicit physician sign-off before consequential clinical actions occur.",
                    sourceLectureId = "lec5_full",
                    sourceCitation = "Lecture 5: Section 5.8 (Slide 118)"
                ),
                KeyPointItem(
                    id = "kp_vibe_coding",
                    topic = "Software Building",
                    conceptTitle = "Vibe Coding (Slide 127)",
                    explanation = "An AI-assisted approach to software development using natural language and AI tools to create, modify, debug, and improve software iteratively. Popularised by Andrej Karpathy in Feb 2025 and named Collins Word of the Year 2025.",
                    clinicalRelevance = "Enables medical students and clinicians to prototype educational apps (like self-test study tools) without manual code writing, while retaining responsibility for validation.",
                    sourceLectureId = "lec5_full",
                    sourceCitation = "Lecture 5: Section 5.9 (Slide 127)"
                ),
                KeyPointItem(
                    id = "kp_ethics_checklist",
                    topic = "Ethics & Compliance",
                    conceptTitle = "Student AI Ethics Checklist (7 Questions) (Slide 144)",
                    explanation = "Before clicking Send, ask: 1) Purpose (Why use AI?), 2) Permission (Allowed for task?), 3) Privacy (Entering sensitive info?), 4) Accuracy (How verify?), 5) Integrity (Represent honestly?), 6) Bias (Unfair assumptions?), 7) Responsibility (Defend final result?).",
                    clinicalRelevance = "Guarantees student academic integrity and protects patient privacy (strictly barring patient names, MRNs, or identifiable data from AI tools).",
                    sourceLectureId = "lec5_full",
                    sourceCitation = "Lecture 5: Section 5.10 (Slide 144)"
                )
            )
        )
    )

    private val customLectures = mutableListOf<LectureMaterial>()

    fun getAllLectures(): List<LectureMaterial> {
        return defaultLectures + customLectures
    }

    fun getLectureById(id: String): LectureMaterial? {
        return getAllLectures().firstOrNull { it.id == id }
    }

    fun addCustomLecture(
        title: String,
        moduleName: String,
        filename: String,
        content: String
    ): LectureMaterial {
        val newId = "custom_${System.currentTimeMillis()}"
        val lines = content.lines()
        val sections = mutableListOf<LectureSection>()
        
        val paragraphs = content.split("\n\n").filter { it.isNotBlank() }
        if (paragraphs.isNotEmpty()) {
            paragraphs.chunked(3).forEachIndexed { index, chunk ->
                val secNum = "C.${index + 1}"
                val secTitle = chunk.firstOrNull()?.take(50)?.replace("\n", " ") ?: "Section $secNum"
                val body = chunk.joinToString("\n\n")
                sections.add(
                    LectureSection(
                        id = "${newId}_sec_${index + 1}",
                        sectionNumber = secNum,
                        title = secTitle,
                        summary = body.take(150) + "...",
                        content = body
                    )
                )
            }
        } else {
            sections.add(
                LectureSection(
                    id = "${newId}_sec_1",
                    sectionNumber = "1.0",
                    title = "Complete Document Content",
                    summary = content.take(150) + "...",
                    content = content
                )
            )
        }

        val lecture = LectureMaterial(
            id = newId,
            title = title,
            courseCode = "SKL 101",
            moduleNumber = 100 + customLectures.size,
            moduleName = moduleName.ifBlank { "User Uploaded Material" },
            filename = filename,
            pageCount = (lines.size / 30).coerceAtLeast(1),
            uploadDate = "Uploaded Recently",
            fullText = content,
            sections = sections,
            keyPoints = emptyList(),
            isCustomUpload = true
        )
        customLectures.add(lecture)
        return lecture
    }

    fun deleteCustomLecture(id: String): Boolean {
        return customLectures.removeAll { it.id == id }
    }

    fun getAllKeyPoints(): List<KeyPointItem> {
        return getAllLectures().flatMap { it.keyPoints }
    }

    fun getCombinedReferenceText(targetLectureId: String? = null): String {
        val lectures = if (targetLectureId != null && targetLectureId != "all") {
            getAllLectures().filter { it.id == targetLectureId }
        } else {
            getAllLectures()
        }
        return lectures.joinToString("\n\n====================\n\n") { lecture ->
            "DOCUMENT: ${lecture.title} (${lecture.courseCode} - ${lecture.moduleName})\nFILENAME: ${lecture.filename}\n\n${lecture.fullText}"
        }
    }
}
