import './App.css';
import { getCardsSorted } from "./model"
import CardsList from "./components/CardsList";

const App = () => (
    <div className="App">
        <header className="App-header">
        </header>
        <CardsList cards={getCardsSorted([], [])} />
    </div>
);

export default App;
