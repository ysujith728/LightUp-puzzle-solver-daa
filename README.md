<h1>💡 LumiNet – Machine Light Up (Akari) Game</h1>

<p>
LumiNet is an intelligent JavaFX-based implementation of the classic 
<b>Light Up (Akari)</b> puzzle game where a <b>human player competes against a CPU</b>.
</p>

<hr>

<h2>🎯 Objective</h2>
<ul>
  <li>Illuminate all white cells using bulbs</li>
  <li>Ensure bulbs do not "see" each other</li>
  <li>Satisfy numbered black cell constraints</li>
</ul>

<hr>

<h2>🧠 Key Features</h2>

<h3>🤖 Intelligent CPU Opponent</h3>
<ul>
  <li>Backtracking-based move selection</li>
  <li>Dynamic Programming (DP) state pruning</li>
  <li>Failure detection for unsolvable states</li>
</ul>

<h3>⚡ Advanced Algorithms Used</h3>
<ul>
  <li><b>Backtracking</b> – explores valid bulb placements</li>
  <li><b>Dynamic Programming</b> – avoids recomputation using memoization</li>
  <li><b>Divide & Conquer</b> – efficient conflict detection & move selection</li>
  <li><b>Greedy Algorithm</b> – puzzle generation & hints</li>
  <li><b>Merge Sort</b> – leaderboard ranking</li>
</ul>

<h3>📊 Performance Analytics</h3>
<ul>
  <li>Recursive calls tracking</li>
  <li>Maximum recursion depth</li>
  <li>DP-pruned states count</li>
  <li>Execution time metrics</li>
</ul>

<h3>🏆 Leaderboard System</h3>
<ul>
  <li>Persistent score storage</li>
  <li>Sorting using Merge Sort (O(n log n))</li>
  <li>Efficiency-based ranking</li>
</ul>

<h3>🎮 Gameplay Features</h3>
<ul>
  <li>User vs CPU turn-based gameplay</li>
  <li>Multiple difficulty levels (5x5 → 13x13)</li>
  <li>Hint system</li>
  <li>Timer tracking</li>
  <li>Board complexity analysis</li>
</ul>

<hr>

<h2>🗂️ Data Structures Used</h2>
<ul>
  <li>2D Arrays – Grid representation</li>
  <li>HashSet – DP memoization</li>
  <li>ArrayList – Leaderboard & metrics</li>
  <li>Graphs (implicit) – visibility relations</li>
  <li>Recursion – solver logic</li>
</ul>

<hr>

<h2>⚙️ Time Complexities</h2>
<table>
  <tr>
    <th>Component</th>
    <th>Complexity</th>
  </tr>
  <tr>
    <td>Backtracking Solver</td>
    <td>O(b<sup>d</sup>)</td>
  </tr>
  <tr>
    <td>DP Lookup</td>
    <td>O(1)</td>
  </tr>
  <tr>
    <td>Conflict Detection (D&C)</td>
    <td>O(n log n)</td>
  </tr>
  <tr>
    <td>Merge Sort (Leaderboard)</td>
    <td>O(n log n)</td>
  </tr>
  <tr>
    <td>Greedy Generation</td>
    <td>O(n²)</td>
  </tr>
</table>

<hr>

<h2>🚀 How to Run</h2>

<ol>
  <li>Ensure Java (JDK 8+) is installed</li>
  <li>Make sure JavaFX is configured</li>
  <li>Compile the program:
    <pre><code>javac LumiLight.java</code></pre>
  </li>
  <li>Run the application:
    <pre><code>java LumiLight</code></pre>
  </li>
</ol>

<hr>

<h2>📈 Unique Highlights</h2>
<ul>
  <li>Hybrid use of <b>Greedy + Backtracking + DP</b></li>
  <li>Real-time performance tracking</li>
  <li>Divide & Conquer applied in gameplay logic</li>
  <li>Game AI with failure-state detection</li>
</ul>

<hr>

<h2>👨‍💻 Author</h2>
<p>
Developed as part of an academic project demonstrating real-world application of:
</p>
<ul>
  <li>Data Structures</li>
  <li>Algorithm Design</li>
  <li>AI-based Game Logic</li>
</ul>

<p><b>Author:</b> Sujith</p>

<hr>

<h2>📌 Notes</h2>
<p>
This project showcases how theoretical concepts like 
<b>Divide & Conquer, Dynamic Programming, and Backtracking</b> 
can be applied in an interactive and competitive gaming environment.
</p>
