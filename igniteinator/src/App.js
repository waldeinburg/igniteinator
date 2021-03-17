import './App.css';
import { getCardsFilteredAndSorted } from "./model"
import CardsList from "./components/CardsList";

const App = () => (
    <div className="App">
        <header className="App-header">
        </header>
        <CardsList cards={getCardsFilteredAndSorted(undefined, [], [])} />
    </div>
);

export default App;
