from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.label import Label
from kivy.uix.button import Button


class GoldAIDashboard(BoxLayout):

    def __init__(self, **kwargs):
        super().__init__(**kwargs)

        self.orientation = "vertical"
        self.padding = 20
        self.spacing = 15

        self.add_widget(
            Label(
                text="LIVE GOLD AI",
                font_size=32
            )
        )

        self.add_widget(
            Label(
                text="XAUUSD AI Trading Engine",
                font_size=20
            )
        )

        self.signal = Label(
            text="MASTER SIGNAL: WAIT",
            font_size=26
        )
        self.add_widget(self.signal)

        self.confidence = Label(
            text="Confidence: -- %",
            font_size=20
        )
        self.add_widget(self.confidence)

        self.add_widget(
            Button(
                text="AI MODELS",
                font_size=20
            )
        )

        self.add_widget(
            Button(
                text="+ ADD AI MODEL",
                font_size=20
            )
        )

        self.add_widget(
            Label(
                text="Engine Status: Ready",
                font_size=18
            )
        )


class LiveGoldAIApp(App):

    def build(self):
        self.title = "Live Gold AI"
        return GoldAIDashboard()


LiveGoldAIApp().run()
